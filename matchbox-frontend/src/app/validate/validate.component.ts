import { AfterViewInit, ChangeDetectorRef, Component } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import pako from 'pako';
import untar from 'js-untar';
import { IDroppedBlob } from '../upload/upload.component';
import ace from 'ace-builds';
import { ValidationEntry } from './validation-entry';
import {ValidationParameter, ValidationParameterDefinition} from './validation-parameter';
import { ITarEntry } from './tar-entry';
import { Issue, OperationResult } from '../util/operation-result';
import { FormControl, Validators } from '@angular/forms';
import { StructureDefinition } from './structure-definition';
import { ToastrService } from 'ngx-toastr';
import {ValidationCodeEditor} from "./validation-code-editor";

const INDENT_SPACES = 2;

@Component({
    selector: 'app-validate',
    templateUrl: './validate.component.html',
    styleUrls: ['./validate.component.scss'],
    standalone: false
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
  validatorSettings: Map<string, ValidationParameterDefinition> = new Map<string, ValidationParameterDefinition>();

  // The input form
  filteredProfiles: Set<StructureDefinition> = new Set<StructureDefinition>();
  profileFilter: string = '';
  selectedIg: string = this.AUTO_IG_SELECTION;
  selectedProfile: string;
  profileControl: FormControl = new FormControl<string>(null, Validators.required);

  // Code editor
  editor: ValidationCodeEditor;
  editorContent: CodeEditorContent = CodeEditorContent.RESOURCE_CONTENT;

  // DOM
  showSettings: boolean = false;
  currentResource: UploadedFile | null = null;

  package: ArrayBuffer;

  constructor(
    data: FhirConfigService,
    private cd: ChangeDetectorRef,
    private toastr: ToastrService,
  ) {
    this.client = data.getFhirClient();

    const validateOperationDefinitionPromise = this.client
          .read({ resourceType: 'OperationDefinition', id: '-s-validate' });

    const implementationGuidesPromise = this.client
      .search({
        resourceType: 'ImplementationGuide',
        searchParams: {
          _sort: 'title',
          _count: 1000, // Load all IGs
        },
      });

    // Wait for the two requests to complete
    Promise.all([validateOperationDefinitionPromise, implementationGuidesPromise])
      .then((values: [fhir.r4.OperationDefinition, fhir.r4.Bundle]) => {
        // Read the server -s-validate OperationDefinition.
        // This will allow us to create the list of supported (installed) profiles, and supported validation parameters.
        this.analyzeValidateOperationDefinition(values[0]);

        // Read the list of installed ImplementationGuides
        values[1].entry
          ?.map((entry: fhir.r4.BundleEntry) => entry.resource as fhir.r4.ImplementationGuide)
          .map((ig: fhir.r4.ImplementationGuide) => `${ig.packageId}#${ig.version}`)
          .sort()
          .forEach((ig) => this.installedIgs.add(ig));

        // Check for query string parameters in the current URL.
        // They may contain a validation request
        this.analyzeUrlForValidation();
      })
      .catch((error) => {
        this.showErrorToast('Network error', error.message);
        console.error(error);
      });
  }

  ngAfterViewInit() {
    // Initializes the code editor, after the DOM is ready
    this.editor = new ValidationCodeEditor(ace.edit('editor'), INDENT_SPACES);
  }

  /**
   * Loads a selected/dropped file in the file selector in Matchbox.
   * @param droppedBlob the selected/dropped file.
   */
  onFileSelected(droppedBlob: IDroppedBlob): void {
    if (droppedBlob.name.endsWith('.tgz')) {
      // Load an IG package
      try {
        this.validateExamplesInPackage(droppedBlob.blob);
      } catch (error) {
        this.showErrorToast('Unexpected error', error.message);
        console.error(error);
      }
      return;
    }

    // We assume that the file is a FHIR resource
    try {
      this.selectedIg = this.AUTO_IG_SELECTION;
      const reader = new FileReader();
      reader.readAsText(droppedBlob.blob);
      reader.onload = () => {
        // need to run CD since file load runs outside of zone
        this.cd.markForCheck();
        this.validateResource(droppedBlob.blob.name, <string>reader.result, droppedBlob.contentType, true);
      };
    } catch (error) {
      this.showErrorToast('Unexpected error', error.message);
      console.error(error);
    }
  }

  validateResource(filename: string,
                   content: string,
                   contentType: string,
                   selectBestProfile: boolean): void {
    let entry: ValidationEntry;
    try {
      // Try to parse the resource to extract information
      entry = new ValidationEntry(filename, content, contentType, null, this.getCurrentValidationSettings());
      this.currentResource = new UploadedFile(
        filename,
        contentType,
        content,
        entry.resourceType
      );
      if (selectBestProfile && entry.selectedProfile) {
        // Auto-select the right profile in the form select
        if (this.supportedProfiles.has(entry.selectedProfile)) {
          // The canonical exists as-is in the list of supported profiles
          this.selectedProfile = entry.selectedProfile;
        } else {
          // The canonical doesn't exist as-is in the list of supported profiles, but it may be present with its
          // version as suffix
          const versionedCanonical = `${entry.selectedProfile}|`;
          for (let [key, _] of this.supportedProfiles) {
            if (key.startsWith(versionedCanonical)) {
              this.selectedProfile = key;
              break;
            }
          }
        }
      }
      this.validationEntries.unshift(entry);
      this.show(entry);
      this.runValidation(entry);
    } catch (error) {
      this.showErrorToast('Error parsing the file', error.message);
      if (entry) {
        entry.result = OperationResult.fromMatchboxError(
          'Error while processing the resource for' + ' validation: ' + error.message
        );
      }
      return;
    }
  }

  /**
   * Analyzes a package file and runs validation for all examples, with the right IG set.
   * @param file the package file to load.
   */
  validateExamplesInPackage(file: File): void {
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
              pointer.runValidation(entry);
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
              let entry = new ValidationEntry(name, JSON.stringify(res, null, 2), 'application/fhir+json', profiles, this.getCurrentValidationSettings());
              dataSource.push(entry);
            }
          }
        );
      }
    };
  }

  /**
   * Clear all history of validation.
   */
  clearAllEntries() {
    this.selectedProfile = null;
    this.selectedIg = this.AUTO_IG_SELECTION;
    this.show(undefined);
    this.validationEntries.splice(0, this.validationEntries.length);
  }

  /**
   * Starts the actual validation of an entry.
   * @param entry the entry to validate.
   */
  runValidation(entry: ValidationEntry) {
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
    for (const param of entry.validationParameters) {
      searchParams.set(param.name, param.value);
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
          this.editor.updateCodeEditorContent(this.selectedEntry, this.editorContent);
        }
      })
      .catch((error) => {
        console.error(error);
        entry.loading = false;
        if (error?.response?.data?.resourceType === 'OperationOutcome') {
          // Got an OperationOutcome, probably with a 500-error code
          entry.setOperationOutcome(error?.response?.data);
          if (entry === this.selectedEntry) {
            this.editor.updateCodeEditorContent(this.selectedEntry, this.editorContent);
          }
        } else if ('message' in error) {
          // Got an error message
          this.showErrorToast('Unexpected error', error.message);
          entry.result = OperationResult.fromMatchboxError(
            'Error while sending the validation request: ' + error.message
          );
          console.error(error);
        } else {
          // Got nothing useful, it seems
          this.showErrorToast('Unknown error', 'Unknown error while sending the validation request');
          entry.result = OperationResult.fromMatchboxError(
            'Unknown error while sending the validation request'
          );
          console.error(error);
        }
      });
  }

  /**
   * Select a validation entry to show in the detail pane.
   * @param entry the validation entry to show, or null to deselect the current one.
   */
  show(entry: ValidationEntry | null) {
    this.selectedEntry = entry;
    this.editor.updateCodeEditorContent(this.selectedEntry, this.editorContent);

    if (entry != null) {
      // Set the resource as currently selected in the form, to facilitate re-validation with a different profile/IG
      this.currentResource = new UploadedFile(entry.filename, entry.mimetype, entry.resource, entry.resourceType);
    }
  }

  /**
   * Remove an entry from the history list.
   * @param entry the entry to remove.
   */
  removeEntryFromHistory(entry: ValidationEntry) {
    if (entry === this.selectedEntry) {
      this.show(null);
    }
    const index = this.validationEntries.indexOf(entry);
    this.validationEntries.splice(index, 1); //remove element from array
  }

  /**
   * Event handler for the click on the "validation" button
   */
  onValidationButtonClick() {
    let entry = new ValidationEntry(
      this.currentResource.filename,
      this.currentResource.content,
      this.currentResource.contentType,
      [this.selectedProfile],
      this.getCurrentValidationSettings()
    );
    if (this.selectedIg != this.AUTO_IG_SELECTION) {
      entry.ig = this.selectedIg;
    }
    this.validationEntries.unshift(entry);
    this.show(entry);
    this.runValidation(entry);
  }

  /**
   * Toggle the display of the settings pane.
   */
  toggleSettings() {
    this.showSettings = !this.showSettings;
  }

  /**
   * Scrolls the code editor to the location of an issue.
   * @param issue the FHIR Issue from an OperationOutcome.
   */
  scrollToIssueLocation(issue: Issue): void {
    if (issue.line && this.editorContent == CodeEditorContent.RESOURCE_CONTENT) {
      // Scroll to the clicked issue, but only if the issue has a location and the resource is shown in the code editor
      // (scrolling in the OperationOutcome would be nonsense).
      this.editor.scrollToIssueLocation(issue);
    }
  }

  /**
   * Updates the list of profiles that are shown in the profile select dropdown by selecting those who contain the
   * search term either in their title or their canonical.
   */
  updateProfileFilter() {
    const searchTerm = this.profileFilter.toLowerCase();
    this.filteredProfiles = new Set<StructureDefinition>(
      [...this.supportedProfiles.values()]
        .filter((sd) => {
          return (
            sd.title.toLocaleLowerCase().includes(searchTerm) || sd.canonical.toLocaleLowerCase().includes(searchTerm)
          );
        })
        .values()
    );
  }

  getDirectLink(entry: ValidationEntry): string {
    const url = new URL(document.location.href);
    url.searchParams.forEach((name: string) => {
      url.searchParams.delete(name);
    });

    url.searchParams.set('resource', btoa(entry.resource));
    url.searchParams.set('profile', entry.selectedProfile);
    if (entry.ig) {
      url.searchParams.set('ig', entry.ig);
    }

    for (const param of entry.validationParameters) {
      url.searchParams.set(param.name, param.value);
    }

    return url.toString();
  }

  copyDirectLink(event: MouseEvent,entry: ValidationEntry) {
    if ('clipboard' in navigator) {
      event.preventDefault();
      const url = this.getDirectLink(entry);
      navigator.clipboard.writeText(url).then(() => {});
    }
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

  changeCodeEditorContent(newContent: CodeEditorContent): void {
    if (this.editorContent === newContent) {
      return;
    }
    this.editorContent = newContent;
    this.editor.updateCodeEditorContent(this.selectedEntry, this.editorContent);
  }

  private getCurrentValidationSettings(): ValidationParameter[] {
    const parameters: ValidationParameter[] = [];
    for (const [_, setting] of this.validatorSettings) {
      if (setting.formControl.value != null && setting.formControl.value.length > 0) {
        parameters.push(new ValidationParameter(setting.param.name, setting.formControl.value));
      }
    }
    return parameters;
  }

  /**
   * Show an error toast message.
   * @param title the toast title.
   * @param message the toast message.
   * @private
   */
  private showErrorToast(title: string, message: string) {
    this.toastr.error(message, title, {
      closeButton: true,
      timeOut: 5000,
    });
  }

  /**
   * Extracts supported validation parameters from the -s-validate OperationDefinition.
   * @param od the -s-validate OperationDefinition.
   * @private
   */
  private analyzeValidateOperationDefinition(od: fhir.r4.OperationDefinition): void {
    od.parameter?.forEach((parameter: fhir.r4.OperationDefinitionParameter) => {
      if (parameter.name == 'profile') {
        parameter._targetProfile?.forEach((item) => {
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
        this.validatorSettings.set(parameter.name, new ValidationParameterDefinition(parameter));
      });
  }

  /**
   * Analyzes the current URL to detect if there is a validation request in the search parameters ('resource',
   * 'profile', others).
   * @private
   */
  private analyzeUrlForValidation(): void {
    if (!window.location.hash) {
      return;
    }
    const searchParams = new URLSearchParams(window.location.hash.substring(1));
    if (searchParams.has('resource')) {
      let hasSetProfile = false;
      if (searchParams.has('profile')) {
        const profile = <string>searchParams.get('profile');
        if (this.supportedProfiles.has(profile)) {
          // The canonical exists as-is in the list of supported profiles
          this.selectedProfile = profile;
          hasSetProfile = true;
        } else {
          this.showErrorToast('Unknown profile', `The profile '${profile}' is unknown to this server. Please choose another profile from the list.`);
          return;
        }
      }

      const resource = atob(searchParams.get('resource'));
      let contentType = 'application/fhir+json';
      if (resource.startsWith('<')) {
        contentType = 'application/fhir+xml';
      }
      const extension = contentType.split('+')[1];

      for (const [key, value] of searchParams) {
        if (key === 'resource' || key === 'profile') {
          continue;
        }
        if (this.validatorSettings.has(key)) {
          this.validatorSettings.get(key).formControl.setValue(value);
        }
      }

      this.validateResource(`provided.${extension}`, resource, contentType, !hasSetProfile);
      this.toastr.info('Validation', 'The validation of your resource has started', {
        closeButton: true,
        timeOut: 3000,
      });
    }
  }
}

/**
 * Struct of an uploaded file.
 */
class UploadedFile {
  constructor(
    public filename: string,
    public contentType: string,
    public content: string,
    public resourceType: string
  ) {}
}

/**
 * Enum of the different tabs in the code editor.
 */
export enum CodeEditorContent {
  RESOURCE_CONTENT,
  OPERATION_OUTCOME,
}
