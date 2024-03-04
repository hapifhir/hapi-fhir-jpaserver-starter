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
import {Issue, IssueSeverity, OperationResult} from '../util/operation-result';
import {FormControl, Validators} from "@angular/forms";

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
  installedIgs: Set<string> = new Set<string>();
  supportedProfiles: Map<string, string> = new Map<string, string>();
  validatorSettings: ValidationParameter[] = new Array<ValidationParameter>();

  // Form
  filteredProfiles: Map<string, string> = new Map<string, string>();
  profileFilter: string = '';
  selectedIg: string = null;
  selectedProfile: string;
  profileControl: FormControl = new FormControl<string>(null, Validators.required);

  // DOM
  editor: Ace.Editor;
  showSettings: boolean = false;
  currentResource: UploadedFile | null = null;
  errorMessage: string | null = null;

  package: ArrayBuffer;

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
                parameter._targetProfile.forEach(item => {
                  let sdCanonical = this.getExtensionStringValue(item, 'sd-canonical');
                  const sdTitle = this.getExtensionStringValue(item, 'sd-title');
                  const igId = this.getExtensionStringValue(item, 'ig-id');
                  const igVersion = this.getExtensionStringValue(item, 'ig-version');
                  let current = '';
                  if (this.getExtensionBoolValue(item, 'ig-current')) {
                    current = ' (current)';
                  } else {
                    sdCanonical += `|${igVersion}`;
                  }

                  this.supportedProfiles.set(sdCanonical, `${sdTitle} — ${igId}#${igVersion}${current}`);
                });
                this.updateProfileFilter();
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
        bundle.entry.map((entry: fhir.r4.BundleEntry) => entry.resource as fhir.r4.ImplementationGuide)
          .map((ig: fhir.r4.ImplementationGuide) => `${ig.packageId}#${ig.version}`)
          .sort()
          .forEach(ig => this.installedIgs.add(ig));
      })
      .catch((error) => {
        this.errorMessage = 'Error accessing FHIR server';
      });
  }

  ngAfterViewInit() {
    this.editor = ace.edit('editor');
    this.editor.setReadOnly(true);
    this.editor.setTheme('ace/theme/textmate');
    this.editor.setOptions({
      tabSize: INDENT_SPACES,
      wrap: true,
      useWorker: false,
      useSvgGutterIcons: false,
    });
  }

  addFile(droppedBlob: IDroppedBlob): void {
    if (droppedBlob.name.endsWith('.tgz')) {
      // Load an IG package
      try {
        this.addPackage(droppedBlob.blob);
      } catch (error) {
        console.error(error);
      }
    } else {
      // We assume that the file is a FHIR resource
      let entry: ValidationEntry = null;
      try {
        this.selectedProfile = null;
        this.selectedIg = null;
        const reader = new FileReader();
        reader.readAsText(droppedBlob.blob);
        reader.onload = () => {
          // need to run CD since file load runs outside of zone
          this.cd.markForCheck();
          entry = new ValidationEntry(droppedBlob.blob.name, <string>reader.result, droppedBlob.contentType, null);
          this.currentResource = new UploadedFile(droppedBlob.name, droppedBlob.contentType, <string>reader.result, entry.resourceType);
          if (entry.selectedProfile) {
            // Auto-select the right profile in the form select
            this.selectedProfile = entry.selectedProfile;
          }
          this.validationEntries.unshift(entry);
          this.show(entry);
          this.validate(entry);
        };
      } catch (error) {
        console.error(error);
        if (entry) {
          entry.result = OperationResult.fromMatchboxError("Error while processing the resource for" +
            " validation: " + error.message);
        }
      }
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
              pointer.validationEntries.unshift(entry);
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
    for (const setting of this.validatorSettings) {
      if (setting.formControl.value != null && setting.formControl.value.length > 0) {
        searchParams.set(setting.param.name, setting.formControl.value);
      }
    }
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
        // Got a response that should be an OperationOutcome
        entry.loading = false;
        entry.setOperationOutcome(response);
        if (entry === this.selectedEntry) {
          this.updateEditorIssues();
        }
      })
      .catch((error) => {
        // fhir-kit-client throws an error when return in not json
        entry.loading = false;
        entry.result = OperationResult.fromMatchboxError("Error while sending the validation request: " +error.message);
        console.error(error);
      });
  }

  show(entry: ValidationEntry | null) {
    this.errorMessage = null;
    this.selectedEntry = entry;
    if (!entry) {
      this.editor.setValue('', -1);
      this.updateEditorIssues();
      return;
    }

    this.currentResource = new UploadedFile(entry.filename, entry.mimetype, entry.resource, entry.resourceType);
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
    let entry = new ValidationEntry(this.currentResource.filename, this.currentResource.content, this.currentResource.contentType, [
      this.selectedProfile,
    ]);
    entry.ig = this.selectedIg;
    this.validationEntries.unshift(entry);
    this.show(entry);
    this.validate(entry);
  }

  toggleSettings() {
    this.showSettings = !this.showSettings;
  }

  updateEditorIssues(): void {
    // Remove old markers
    this.editor.session.clearAnnotations();

    if (!this.selectedEntry?.result) {
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

  updateProfileFilter() {
    this.filteredProfiles = new Map<string, string>(
      [...this.supportedProfiles].filter(([_, title]) => title.includes(this.profileFilter))
    );
  }

  getExtensionStringValue(element: fhir.r4.Element, url: string): string {
    return this.getExtension(element, url)?.valueString ?? '';
  }

  getExtensionBoolValue(element: fhir.r4.Element, url: string): boolean {
    return this.getExtension(element, url)?.valueBoolean ?? false;
  }

  getExtension(element: fhir.r4.Element, url: string): fhir.r4.Extension {
    for (let i = 0; i < element.extension.length; i++) {
      if (element.extension[i].url === url) {
        return element.extension[i];
      }
    }
    return null;
  }
}

class UploadedFile {
  constructor(public filename: string, public contentType: string, public content: string, public resourceType: string) {}
}
