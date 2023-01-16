import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import { FormControl } from '@angular/forms';
import { buffer, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import pako from 'pako';
import untar from 'js-untar';
import { MatTableDataSource } from '@angular/material/table';
import { DataSource } from '@angular/cdk/collections';
import { timeStamp } from 'console';
import { HighlightSpanKind } from 'typescript';
import { IDroppedBlob } from '../upload/upload.component';

interface ITarEntry {
  name: string; // "package/package.json",
  mode: string; // "0100644 ",
  uid: number; // 0,
  gid: number; // 0,
  size: number; // 647,
  mtime: number; // 1641566058,
  checksum: number; // 13500,
  type: string; // "0",
  linkname: string; // "",
  ustarFormat: string; // "ustar",
  version: string; // "00",
  uname: string; // "",
  gname: string; // "",
  devmajor: number; //0,
  devminor: number; //0,
  namePrefix: string; // "",
  buffer: ArrayBuffer; // {}
  getBlobUrl: () => string; // ???
  readAsJSON: () => any;
  readAsString: () => string;
}

class ValidationParameter {
  param: fhir.r4.OperationDefinitionParameter;
  valueBoolean: boolean;
  valueString;
  String;
  formControl: FormControl;

  constructor(param: fhir.r4.OperationDefinitionParameter) {
    this.param = param;
    this.formControl = new FormControl();
  }

  isValueSet(): boolean {
    return this.valueBoolean != null || this.valueString != null;
  }
}

class ValidationEntry {
  name: string; // "package/package.json",
  json: string;
  mimetype: string;
  operationOutcome: fhir.r4.OperationOutcome;
  profiles: string[];
  public ig: string;
  public fhirVersion: string;

  constructor(
    name: string,
    json: string,
    mimetype: string,
    profiles: string[]
  ) {
    this.name = name;
    this.json = json;
    this.mimetype = mimetype;
    this.profiles = profiles;
  }

  getErrors(): number {
    if (this.operationOutcome) {
      return this.operationOutcome?.issue?.filter(
        (issue) =>
          issue.code === 'processing' &&
          (issue.severity === 'error' || issue.severity === 'fatal')
      ).length;
    }
    return undefined;
  }

  getWarnings(): number {
    if (this.operationOutcome) {
      return this.operationOutcome?.issue?.filter(
        (issue) => issue.code === 'processing' && issue.severity === 'warning'
      ).length;
    }
    return undefined;
  }

  getInfos(): number {
    if (this.operationOutcome) {
      return this.operationOutcome?.issue?.filter(
        (issue) =>
          issue.code === 'processing' && issue.severity === 'information'
      ).length;
    }
    return undefined;
  }
}
@Component({
  selector: 'app-validate',
  templateUrl: './validate.component.html',
  styleUrls: ['./validate.component.scss'],
})
export class ValidateComponent implements OnInit {
  json: string;
  capabilitystatement: fhir.r4.CapabilityStatement;
  client: FhirClient;
  errMsg: string;
  operationOutcome: fhir.r4.OperationOutcome;
  package: ArrayBuffer;
  resourceName: string;
  resourceId: string;
  selectedProfile: string;
  validationInProgress: number;
  selectedEntry: ValidationEntry;
  profiles: string[];
  igs: string[];
  selectedIg: string = null;
  validatorSettings: ValidationParameter[] = new Array<ValidationParameter>();

  dataSource = new MatTableDataSource<ValidationEntry>();
  showSettings: boolean = false;

  constructor(private data: FhirConfigService, private cd: ChangeDetectorRef) {
    this.client = data.getFhirClient();

    this.client
      .capabilityStatement()
      .then((data: fhir.r4.CapabilityStatement) => {
        this.capabilitystatement = data;
        // TODO read operation definition id out of capability statement
        this.client
          .read({ resourceType: 'OperationDefinition', id: '-s-validate' })
          .then((od: fhir.r4.OperationDefinition) => {
            od.parameter?.forEach(
              (parameter: fhir.r4.OperationDefinitionParameter) => {
                if (parameter.name == 'profile') {
                  this.profiles = parameter.targetProfile;
                }
              }
            );
            od.parameter
              .filter(
                (f) =>
                  f.use == 'in' &&
                  f.name != 'resource' &&
                  f.name != 'profile' &&
                  f.name != 'ig'
              )
              .forEach((parameter: fhir.r4.OperationDefinitionParameter) => {
                this.validatorSettings.push(new ValidationParameter(parameter));
              });
          });
      })
      .catch((error) => {
        this.errMsg = 'Error accessing FHIR server';
        this.operationOutcome = error.response.data;
      });

    this.client
      .search({
        resourceType: 'ImplementationGuide',
        searchParams: {
          _sort: 'title',
        },
      })
      .then((bundle: fhir.r4.Bundle) => {
        this.igs = bundle.entry
          .map(
            (entry) =>
              (<fhir.r4.ImplementationGuide>entry.resource).packageId +
              '#' +
              (<fhir.r4.ImplementationGuide>entry.resource).version
          )
          .sort();
      })
      .catch((error) => {
        this.errMsg = 'Error accessing FHIR server';
        this.operationOutcome = error.response.data;
      });

    this.validationInProgress = 0;
  }

  getSelectedProfile(): string {
    return this.selectedProfile;
  }

  setSelectedProfile(value: string) {
    this.selectedProfile = value;
  }

  getSelectedIg(): string {
    return this.selectedIg;
  }

  setSelectedIg(value: string) {
    this.selectedIg = value;
  }

  getProfiles(): string[] {
    return this.profiles;
  }

  addFile(droppedBlob: IDroppedBlob) {
    this.validationInProgress += 1;
    if (
      droppedBlob.contentType === 'application/json' ||
      droppedBlob.name.endsWith('.json')
    ) {
      this.addJson(droppedBlob.blob);
    }
    if (
      droppedBlob.contentType === 'application/xml' ||
      droppedBlob.name.endsWith('.xml')
    ) {
      this.addXml(droppedBlob.blob);
    }
    if (droppedBlob.name.endsWith('.tgz')) {
      this.addPackage(droppedBlob.blob);
    }
    this.validationInProgress -= 1;
  }

  addXml(file) {
    this.selectedProfile = null;
    this.selectedIg = null;
    const reader = new FileReader();
    reader.readAsText(file);
    const dataSource = this.dataSource;
    reader.onload = () => {
      // need to run CD since file load runs outside of zone
      this.cd.markForCheck();
      let entry = new ValidationEntry(
        file.name,
        <string>reader.result,
        'application/fhir+xml',
        null
      );
      this.validate(entry);
    };
  }

  addJson(file) {
    this.selectedProfile = null;
    this.selectedIg = null;
    const reader = new FileReader();
    reader.readAsText(file);
    const dataSource = this.dataSource;
    reader.onload = () => {
      // need to run CD since file load runs outside of zone
      this.cd.markForCheck();
      let entry = new ValidationEntry(
        file.name,
        <string>reader.result,
        'application/fhir+json',
        null
      );
      this.selectRow(entry);
      if (this.selectedProfile != null) {
        entry.profiles = [this.selectedProfile];
      }
      this.validate(entry);
    };
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
    const contentType = res.headers.get('Content-Type');
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
              entry.ig = ig;
              entry.fhirVersion = fhirVersion;
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
            if (
              extractedFile.name?.indexOf('example') >= 0 &&
              extractedFile.name?.indexOf('.index.json') == -1
            ) {
              let name = extractedFile.name;
              if (name.startsWith('package/example/')) {
                name = name.substring('package/example/'.length);
              }
              if (name.startsWith('example/')) {
                name = name.substring('example/'.length);
              }
              let decoder = new TextDecoder('utf-8');
              let res = JSON.parse(
                decoder.decode(extractedFile.buffer)
              ) as fhir.r4.Resource;
              let profiles = res.meta?.profile;
              // maybe better add ig as a parmeter, we assume now that ig version is equal to canonical version
              for (let i = 0; i < profiles.length; i++) {
                profiles[i] = profiles[i];
              }
              let entry = new ValidationEntry(
                name,
                JSON.stringify(res, null, 2),
                'application/fhir+json',
                profiles
              );
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
    this.selectRow(undefined);
    const len = this.dataSource.data.length;
    this.dataSource.data.splice(0, len);
    this.dataSource.data = this.dataSource.data; // https://stackoverflow.com/questions/46746598/angular-material-how-to-refresh-a-data-source-mat-table
  }

  validate(row: ValidationEntry) {
    if (this.selectedProfile != null) {
      row.profiles = [this.selectedProfile];
    }
    if (this.selectedIg != null) {
      if (this.selectedIg.endsWith(' (current)')) {
        row.ig = this.selectedIg.substring(0, this.selectedIg.length - 10);
      } else {
        row.ig = this.selectedIg;
      }
    }

    let valprofile = '';
    try {
      if (row.profiles?.length > 0) {
        valprofile = '?profile=' + encodeURIComponent(row.profiles[0]);
        if (row.ig != null) {
          valprofile += '&ig=' + encodeURIComponent(row.ig);
        }
      } else {
        return;
      }
    } catch (error) {}
    // for each validatorSetting we add url paramter to valprofile
    for (let i = 0; i < this.validatorSettings.length; i++) {
      if (
        this.validatorSettings[i].formControl.value != null &&
        this.validatorSettings[i].formControl.value.length > 0
      ) {
        valprofile +=
          '&' +
          this.validatorSettings[i].param.name +
          '=' +
          encodeURIComponent(this.validatorSettings[i].formControl.value);
      }
    }
    this.validationInProgress += 1;
    this.client
      .operation({
        name: 'validate' + valprofile,
        resourceType: undefined,
        input: row.json,
        options: {
          headers: {
            accept: 'application/fhir+json',
            'content-type': row.mimetype,
          },
        },
      })
      .then((response) => {
        // see below
        this.validationInProgress -= 1;
        row.operationOutcome = response;
        this.dataSource.data.push(row);
        this.dataSource.data = this.dataSource.data; // https://stackoverflow.com/questions/46746598/angular-material-how-to-refresh-a-data-source-mat-table
        if (this.validationInProgress == 0) {
          this.selectRow(row);
        }
      })
      .catch((error) => {
        // fhir-kit-client throws an error when  return in not json
        this.validationInProgress -= 1;
      });
  }

  selectRow(row: ValidationEntry) {
    this.selectedEntry = row;
    if (row) {
      this.operationOutcome = row.operationOutcome;
      this.json = row.json;
      this.resourceName = '';
      this.resourceId = '';
      if (row.mimetype === 'application/fhir+json') {
        const res = <fhir.r4.Resource>JSON.parse(this.json);
        if (res?.resourceType) {
          this.resourceName = res.resourceType;
          this.resourceId = res.id;
        }
        this.selectedProfile = res.meta?.profile?.[0];
      }
      if (row.mimetype === 'application/fhir+xml') {
        let pos = this.json.indexOf('<?') + 1;
        let posLeft = this.json.indexOf('<', pos);
        let posRight = this.json.indexOf('>', posLeft);
        if (posLeft < posRight) {
          let tag = this.json.substring(posLeft + 1, posRight - 1);
          let posTag = tag.indexOf(' xmlns');
          if (posTag > 0) {
            tag = tag.substring(0, posTag);
          }
          posTag = tag.indexOf(':');
          if (posTag > 0) {
            tag = tag.substring(posTag + 1);
          }
          this.resourceName = tag;
        }
      }
    } else {
      this.operationOutcome = undefined;
      this.json = undefined;
    }
  }

  remove(row: ValidationEntry) {
    const index = this.dataSource.data.indexOf(row);
    this.dataSource.data.splice(index, 1); //remove element from array
    this.dataSource.data = this.dataSource.data; // https://stackoverflow.com/questions/46746598/angular-material-how-to-refresh-a-data-source-mat-table
  }

  validationOutcomeTitle(): string {
    return `Details Validation Results ${this.resourceName} / ${this.resourceId}`;
  }

  onValidate() {
    let entry = new ValidationEntry(
      this.selectedEntry.name,
      this.selectedEntry.json,
      this.selectedEntry.mimetype,
      [this.selectedProfile]
    );
    this.validate(entry);
  }

  getJson(): String {
    return this.json;
  }
  ngOnInit(): void {}

  toggleSettings() {
    this.showSettings = !this.showSettings;
  }

  getLocalStorageItemOrDefault(key: string, def: string): string {
    const val: string = localStorage.getItem(key);
    if (val) {
      return val;
    }
    return def;
  }

  setLocaleStorageItem(key: string, value: string): string {
    localStorage.setItem(key, value);
    return value;
  }
}
