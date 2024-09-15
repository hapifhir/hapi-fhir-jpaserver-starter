import { AfterViewInit, ChangeDetectorRef, Component } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import pako from 'pako';
import untar from 'js-untar';
import { IDroppedBlob } from '../upload/upload.component';
import ace, { Ace } from 'ace-builds';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/mode-xml';
import { ValidationEntry } from './validation-entry';
import { ValidationParameter } from './validation-parameter';
import { ITarEntry } from './tar-entry';
import {Issue, IssueSeverity, OperationResult} from '../util/operation-result';
import {FormControl, Validators} from "@angular/forms";
import {StructureDefinition} from "./structure-definition";
import {ToastrService} from "ngx-toastr";

const INDENT_SPACES = 2;

@Component({
  selector: 'app-validate',
  templateUrl: './validate.component.html',
  styleUrls: ['./validate.component.scss'],
})
export class ValidateComponent implements AfterViewInit {
  readonly AUTO_IG_SELECTION = 'AUTOMATIC';
  readonly CodeEditorContent = CodeEditorContent;

  // Validation history
  validationEntries: ValidationEntry[] = new Array<ValidationEntry>();
  selectedEntry: ValidationEntry | null = null;

  // About the server
  client: FhirClient;
  capabilityStatement: fhir.r4.CapabilityStatement | null = null;
  installedIgs: Set<string> = new Set<string>();
  supportedProfiles: Map<string, StructureDefinition> = new Map<string, StructureDefinition>();
  validatorSettings: ValidationParameter[] = new Array<ValidationParameter>();

  // The input form
  filteredProfiles: Set<StructureDefinition> = new Set<StructureDefinition>();
  profileFilter: string = '';
  selectedIg: string = this.AUTO_IG_SELECTION;
  selectedProfile: string;
  profileControl: FormControl = new FormControl<string>(null, Validators.required);

  // Code editor
  editor: Ace.Editor;
  editorContent: CodeEditorContent = CodeEditorContent.RESOURCE_CONTENT;

  // DOM
  showSettings: boolean = false;
  currentResource: UploadedFile | null = null;
  errorMessage: string | null = null;

  package: ArrayBuffer;

  constructor(
    data: FhirConfigService,
    private cd: ChangeDetectorRef,
    private toastr: ToastrService,
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
                  const sd = new StructureDefinition();
                  sd.canonical = this.getExtensionStringValue(item, 'sd-canonical');
                  sd.title = this.getExtensionStringValue(item, 'sd-title');
                  sd.igId = this.getExtensionStringValue(item, 'ig-id');
                  sd.igVersion = this.getExtensionStringValue(item, 'ig-version');
                  sd.isCurrent = false;

                  if (this.getExtensionBoolValue(item, 'ig-current')) {
                    sd.isCurrent = true;
                  } else {
                    sd.canonical += `|${sd.igVersion}`;
                  }

                  this.supportedProfiles.set(sd.canonical, sd);
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
    this.editor.commands.removeCommand('find');
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
        this.selectedIg = this.AUTO_IG_SELECTION;
        const reader = new FileReader();
        reader.readAsText(droppedBlob.blob);
        reader.onload = () => {
          try {
            // need to run CD since file load runs outside of zone
            this.cd.markForCheck();
            // Try to parse the resource to extract information
            entry = new ValidationEntry(droppedBlob.blob.name, <string>reader.result, droppedBlob.contentType, null);
            this.currentResource = new UploadedFile(droppedBlob.name, droppedBlob.contentType, <string>reader.result, entry.resourceType);
            if (entry.selectedProfile) {
              // Auto-select the right profile in the form select
              if (this.supportedProfiles.has(entry.selectedProfile)) {
                // The canonical exists as-is in the list of supported profiles
                this.selectedProfile = entry.selectedProfile;
              } else {
                // The canonical doesn't exist as-is in the list of supported profiles, but it may be present with its
                // version as suffix
                const versionedCanonical = `${entry.selectedProfile}|`;
                for (let [key, value] of this.supportedProfiles) {
                  if (key.startsWith(versionedCanonical)) {
                    this.selectedProfile = key;
                    break;
                  }
                }
              }
            }
            this.validationEntries.unshift(entry);
            this.show(entry);
            this.validate(entry);
          } catch (error) {
            this.showErrorToast('Error parsing the file', error.message);
            if (entry) {
              entry.result = OperationResult.fromMatchboxError("Error while processing the resource for" +
                " validation: " + error.message);
            }
            return;
          }
        };
      } catch (error) {
        this.showErrorToast('Unexpected error', error.message);
        console.error(error);
      }
    }
  }

  onValidateIg() {
    let igid: string = '';

    if (this.selectedIg != this.AUTO_IG_SELECTION) {
      if (this.selectedIg.endsWith(' (last)')) {
        igid = this.selectedIg.substring(0, this.selectedIg.length - 7);
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
    this.selectedIg = this.AUTO_IG_SELECTION;
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
            this.showErrorToast('Unexpected error', err);
            console.error(err);
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
    this.selectedIg = this.AUTO_IG_SELECTION;
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

    if (this.selectedIg != this.AUTO_IG_SELECTION) {
      if (this.selectedIg.endsWith(' (last)')) {
        entry.ig = this.selectedIg.substring(0, this.selectedIg.length - 7);
      } else {
        entry.ig = this.selectedIg;
      }
    }

    if (!entry.selectedProfile) {
      this.showErrorToast('Validation failed', 'No profile was selected');
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
          this.updateCodeEditorContent();
        }
      })
      .catch((error) => {
        // fhir-kit-client throws an error when return in not json
        entry.loading = false;
        this.showErrorToast('Unexpected error', error.message);
        entry.result = OperationResult.fromMatchboxError("Error while sending the validation request: " +error.message);
        console.error(error);
      });
  }

  show(entry: ValidationEntry | null) {
    this.errorMessage = null;
    this.selectedEntry = entry;
    this.updateCodeEditorContent();

    if (entry != null) {
      this.currentResource = new UploadedFile(entry.filename, entry.mimetype, entry.resource, entry.resourceType);
    }
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
    if (this.selectedIg != this.AUTO_IG_SELECTION) {
      entry.ig = this.selectedIg;
    }
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

  highlightIssue(issue: Issue): void {
    if (issue.line && this.editorContent == CodeEditorContent.RESOURCE_CONTENT) {
      this.editor.gotoLine(issue.line, issue.col, true);
      this.editor.scrollToLine(issue.line, false, true, () => {});
    }
  }

  /**
   * Updates the list of profiles that are shown in the profile select dropdown by selecting those who contain the
   * search term either in their title or their canonical.
   */
  updateProfileFilter() {
    const searchTerm = this.profileFilter.toLowerCase();
    this.filteredProfiles = new Set<StructureDefinition>(
      [...this.supportedProfiles.values()].filter((sd) => {
        return sd.title.toLocaleLowerCase().includes(searchTerm) || sd.canonical.toLocaleLowerCase().includes(searchTerm);
      }).values()
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

  updateCodeEditorContent(): void {
    if (!this.selectedEntry) {
      this.editor.setValue('', -1);
      this.editor.session.clearAnnotations();
      return;
    }

    if (this.editorContent == CodeEditorContent.RESOURCE_CONTENT) {
      this.editor.setValue(this.selectedEntry.resource, -1);
      if (this.selectedEntry.mimetype === 'application/fhir+json') {
        this.editor.getSession().setMode('ace/mode/json');
      } else if (this.selectedEntry.mimetype === 'application/fhir+xml') {
        this.editor.getSession().setMode('ace/mode/xml');
      }
      this.updateEditorIssues();
    } else {
      if (this.selectedEntry.result !== undefined && 'operationOutcome' in this.selectedEntry.result) {
        this.editor.setValue(JSON.stringify(this.selectedEntry.result.operationOutcome, null, INDENT_SPACES), -1);
        this.editor.getSession().setMode('ace/mode/json');
      } else {
        this.editor.setValue('', -1);
      }
      this.editor.session.clearAnnotations();
    }
  }

  changeCodeEditorContent(newContent: CodeEditorContent): void {
    if (this.editorContent === newContent) {
      return;
    }
    this.editorContent = newContent;
    this.updateCodeEditorContent();
  }

  showErrorToast(title: string, message: string) {
    this.toastr.error(message, title, {
      closeButton: true,
      timeOut: 5000,
    });
  }
}

class UploadedFile {
  constructor(public filename: string, public contentType: string, public content: string, public resourceType: string) {}
}

enum CodeEditorContent {
  RESOURCE_CONTENT,
  OPERATION_OUTCOME,
}
