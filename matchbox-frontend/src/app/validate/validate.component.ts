import { AfterViewInit, ChangeDetectorRef, Component } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import pako from 'pako';
import untar from 'js-untar';
import { IDroppedBlob } from '../upload/upload.component';
import ace, { Ace } from 'ace-builds';
import { ValidationEntry } from './validation-entry';
import { ValidationParameter } from './validation-parameter';
import { ITarEntry } from './tar-entry';
import { Issue, IssueSeverity } from '../util/operation-result';

const INDENT_SPACES = 4;

@Component({
  selector: 'app-validate',
  templateUrl: './validate.component.html',
  styleUrls: ['./validate.component.scss'],
})
export class ValidateComponent implements AfterViewInit {

  // Validation history
  validationEntries: ValidationEntry[] = new Array<ValidationEntry>();
  selectedEntry: ValidationEntry | null = null;

  // About the server
  client: FhirClient;
  capabilityStatement: fhir.r4.CapabilityStatement | null = null;
  installedIgs: string[] = new Array<string>();
  validatorSettings: ValidationParameter[] = new Array<ValidationParameter>();

  // Form
  selectedIg: string = null;
  selectedProfile: string;

  // DOM
  editor: Ace.Editor;
  showSettings: boolean = false;




  errorMessage: string | null = null;
  package: ArrayBuffer;
  resourceName: string;
  resourceId: string;
  validationInProgress: number;
  profiles: string[] = new Array<string>();
  json: string;


  constructor(
    data: FhirConfigService,
    private cd: ChangeDetectorRef
  ) {
    this.client = data.getFhirClient();

    this.client
      .capabilityStatement()
      .then((data: fhir.r4.CapabilityStatement) => {
        this.capabilityStatement = data;
        // TODO read operation definition id out of capability statement
        this.client
          .read({ resourceType: 'OperationDefinition', id: '-s-validate' })
          .then((od: fhir.r4.OperationDefinition) => {
            od.parameter?.forEach((parameter: fhir.r4.OperationDefinitionParameter) => {
              if (parameter.name == 'profile') {
                this.profiles.push(...parameter.targetProfile);
              }
            });
            od.parameter
              .filter((f) => f.use == 'in' && f.name != 'resource' && f.name != 'profile' && f.name != 'ig')
              .forEach((parameter: fhir.r4.OperationDefinitionParameter) => {
                this.validatorSettings.push(new ValidationParameter(parameter));
              });
          });
      })
      .catch((error) => {
        this.errorMessage = 'Error accessing FHIR server';
      });

    this.client
      .search({
        resourceType: 'ImplementationGuide',
        searchParams: {
          _sort: 'title',
          _count: 1000, // Load all IGs
        },
      })
      .then((bundle: fhir.r4.Bundle) => {
        this.installedIgs = bundle.entry
          .map(
            (entry) =>
              (<fhir.r4.ImplementationGuide>entry.resource).packageId +
              '#' +
              (<fhir.r4.ImplementationGuide>entry.resource).version
          )
          .sort();
      })
      .catch((error) => {
        this.errorMessage = 'Error accessing FHIR server';
      });

    this.validationInProgress = 0;
  }

  ngAfterViewInit() {
    this.editor = ace.edit('editor');
    this.editor.setReadOnly(true);
    //this.editor.setValue(JSON.stringify(data, null, INDENT_SPACES), -1);
    this.editor.setTheme('ace/theme/textmate');
    this.editor.setOptions({
      maxLines: 10000,
      tabSize: INDENT_SPACES,
      wrap: true,
      useWorker: false,
      useSvgGutterIcons: false,
    });
    //this.editor.resize(true);
  }

  addFile(droppedBlob: IDroppedBlob): void {
    try {
      this.validationInProgress += 1;
      if (droppedBlob.name.endsWith('.tgz')) {
        // Load an IG package
        this.addPackage(droppedBlob.blob);
      } else {
        // We assume that the file is a FHIR resource
        this.selectedProfile = null;
        this.selectedIg = null;
        const reader = new FileReader();
        reader.readAsText(droppedBlob.blob);
        reader.onload = () => {
          // need to run CD since file load runs outside of zone
          this.cd.markForCheck();
          const entry = new ValidationEntry(droppedBlob.blob.name, <string>reader.result, droppedBlob.contentType, null);
          if (entry.selectedProfile) {
            this.selectedProfile = entry.selectedProfile;
          }
          this.validationEntries.push(entry);
          this.show(entry);
          this.validate(entry);
        };
      }
      this.validationInProgress -= 1;
    } catch (error) {
      console.error(error);
    }
  }

  onValidateIg() {
    let igid: string = '';

    if (this.selectedIg != null) {
      if (this.selectedIg.endsWith(' (current)')) {
        igid = this.selectedIg.substring(0, this.selectedIg.length - 10);
      } else {
        igid = this.selectedIg;
      }
      igid = igid.replace('#', '-');
      this.fetchData(this.client.baseUrl + '/ImplementationGuide/' + igid);
    }
  }

  async fetchData(url: string) {
    const res = await fetch(url, {
      cache: 'no-store',
      headers: {
        Accept: 'application/gzip',
      },
    });
    const blob = await res.blob();
    this.addPackage(blob);
  }

  addPackage(file) {
    this.selectedProfile = null;
    this.selectedIg = null;
    const reader = new FileReader();
    reader.readAsArrayBuffer(file);
    reader.onload = () => {
      this.package = <ArrayBuffer>reader.result;
      // need to run CD since file load runs outside of zone
      this.cd.markForCheck();
      if (this.package != null) {
        const result = pako.inflate(new Uint8Array(this.package));
        const dataSource = new Array<ValidationEntry>();
        let fhirVersion: string = null;
        let ig: string = null;
        const pointer = this;
        untar(result.buffer).then(
          function (extractedFiles) {
            // onSuccess
            dataSource.forEach((entry) => {
              pointer.validate(entry);
            });
          },
          function (err) {
            // onError
          },
          function (extractedFile: ITarEntry) {
            // onProgress
            if (extractedFile.name?.indexOf('package.json') >= 0) {
              let decoder = new TextDecoder('utf-8');
              let res = JSON.parse(decoder.decode(extractedFile.buffer));
              fhirVersion = res['fhirVersions'][0];
              ig = res['name'] + '#' + res['version'];
            }
            if (extractedFile.name?.indexOf('example') >= 0 && extractedFile.name?.indexOf('.index.json') == -1) {
              let name = extractedFile.name;
              if (name.startsWith('package/example/')) {
                name = name.substring('package/example/'.length);
              }
              if (name.startsWith('example/')) {
                name = name.substring('example/'.length);
              }
              let decoder = new TextDecoder('utf-8');
              let res = JSON.parse(decoder.decode(extractedFile.buffer)) as fhir.r4.Resource;
              let profiles = res.meta?.profile;
              // maybe better add ig as a parmeter, we assume now that ig version is equal to canonical version
              for (let i = 0; i < profiles.length; i++) {
                profiles[i] = profiles[i];
              }
              let entry = new ValidationEntry(name, JSON.stringify(res, null, 2), 'application/fhir+json', profiles);
              dataSource.push(entry);
            }
          }
        );
      }
    };
  }

  onClear() {
    this.selectedProfile = null;
    this.selectedIg = null;
    this.show(undefined);
    this.validationEntries.splice(0, this.validationEntries.length);
  }

  validate(entry: ValidationEntry) {
    if (this.selectedProfile != null) {
      if (!entry.profiles.includes(this.selectedProfile)) {
        entry.profiles.push(this.selectedProfile);
      }
      entry.selectedProfile = this.selectedProfile;
    }

    if (this.selectedIg != null) {
      if (this.selectedIg.endsWith(' (current)')) {
        entry.ig = this.selectedIg.substring(0, this.selectedIg.length - 10);
      } else {
        entry.ig = this.selectedIg;
      }
    }

    if (!entry.selectedProfile) {
      console.error("No profile selected, won't run validation");
      return;
    }

    const searchParams = new URLSearchParams();
    searchParams.set('profile', entry.selectedProfile);
    if (entry.ig) {
      searchParams.set('ig', entry.ig);
    }

    // Validation options
    for (let i = 0; i < this.validatorSettings.length; i++) {
      if (
        this.validatorSettings[i].formControl.value != null &&
        this.validatorSettings[i].formControl.value.length > 0
      ) {
        searchParams.set(this.validatorSettings[i].param.name, this.validatorSettings[i].formControl.value);
      }
    }
    this.validationInProgress += 1;
    entry.loading = true;
    this.client
      .operation({
        name: 'validate?' + searchParams.toString(),
        resourceType: undefined,
        input: entry.resource,
        options: {
          headers: {
            accept: 'application/fhir+json',
            'content-type': entry.mimetype,
          },
        },
      })
      .then((response) => {
        // see below
        this.validationInProgress -= 1;
        entry.loading = false;
        entry.setOperationOutcome(response);
        if (this.validationInProgress == 0) {
          this.show(entry);
        } else {
          this.updateEditorIssues();
        }
      })
      .catch((error) => {
        // fhir-kit-client throws an error when  return in not json
        this.validationInProgress -= 1;
        entry.loading = false;
        console.error(error);
      });
  }

  show(entry: ValidationEntry | null) {
    this.errorMessage = null;
    this.selectedEntry = entry;
    if (!entry) {
      this.json = null;
      this.editor.setValue('', -1);
      this.updateEditorIssues();
      return;
    }

    this.json = entry.resource;
    this.resourceName = '';
    this.resourceId = '';
    this.editor.setValue(entry.resource, -1);
    if (entry.mimetype === 'application/fhir+json') {
      this.editor.getSession().setMode('ace/mode/json');
    } else if (entry.mimetype === 'application/fhir+xml') {
      this.editor.getSession().setMode('ace/mode/xml');
    }
    this.updateEditorIssues();
  }

  removeEntryFromHistory(entry: ValidationEntry) {
    if (entry === this.selectedEntry) {
      this.show(null);
    }
    const index = this.validationEntries.indexOf(entry);
    this.validationEntries.splice(index, 1); //remove element from array
  }

  onValidate() {
    let entry = new ValidationEntry(this.selectedEntry.filename, this.selectedEntry.resource, this.selectedEntry.mimetype, [
      this.selectedProfile,
    ]);
    this.validationEntries.push(entry);
    this.validate(entry);
  }

  toggleSettings() {
    this.showSettings = !this.showSettings;
  }

  updateEditorIssues(): void {
    // Remove old markers
    this.editor.session.clearAnnotations();

    if (!this.selectedEntry || !this.selectedEntry.result) {
      return;
    }
    // Add new markers
    const annotations = this.selectedEntry.result.issues
      .filter(issue => issue.line)
      .map(issue => {
        let type;
        switch (issue.severity) {
          case IssueSeverity.Fatal:
          case IssueSeverity.Error:
            type = 'error';
            break;
          case IssueSeverity.Warning:
            type = 'warning';
            break;
          case IssueSeverity.Information:
            type = 'info';
            break;
        }
        return {
          row: issue.line - 1,
          column: issue.col,
          text: issue.text,
          type
        };
      });
    this.editor.session.setAnnotations(annotations);
  }

  highlightIssue(issue: Issue) {
    if (issue.line) {
      this.editor.gotoLine(issue.line, issue.col, true);
      this.editor.scrollToLine(issue.line, false, true, () => {});
    }
  }
}
