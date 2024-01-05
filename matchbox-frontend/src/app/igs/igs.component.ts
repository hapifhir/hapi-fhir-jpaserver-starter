import { Component } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import { UntypedFormControl, Validators } from '@angular/forms';
import FhirClient from 'fhir-kit-client';
import { FhirPathService } from '../fhirpath.service';
import {OperationResult} from "../util/operation-result";

@Component({
  selector: 'app-igs',
  templateUrl: './igs.component.html',
  styleUrls: ['./igs.component.scss'],
})
export class IgsComponent {
  public addPackageId: UntypedFormControl;
  public addVersion: UntypedFormControl;
  public addUrl: UntypedFormControl;
  public selection: fhir.r4.ImplementationGuide;

  update: boolean = false;

  client: FhirClient;
  igs: fhir.r4.ImplementationGuide[];
  errorMessage: string;
  operationResult: OperationResult | null = null;

  query = {
    _sort: 'title',
    _count: 10000,
  };

  constructor(
    private data: FhirConfigService,
    private fhirPathService: FhirPathService
  ) {
    this.client = data.getFhirClient();
    this.addPackageId = new UntypedFormControl('', [Validators.required, Validators.minLength(1)]);
    this.addVersion = new UntypedFormControl('current', [Validators.required, Validators.minLength(1)]);
    this.addUrl = new UntypedFormControl('url');
    this.search();
  }

  search() {
    this.client
      .search({ resourceType: 'ImplementationGuide', searchParams: this.query })
      .then((response) => {
        const bundle = <fhir.r4.Bundle>response;
        this.igs = bundle.entry.map((entry) => <fhir.r4.ImplementationGuide>entry.resource);
        this.selection = undefined;
        this.addPackageId.setValue('');
        this.addVersion.setValue('');
        this.addUrl.setValue('');
      })
      .catch((error) => {
        this.errorMessage = 'Error accessing FHIR server';
        if (error.response?.data) {
          this.operationResult = OperationResult.fromOperationOutcome(error.response.data);
        } else {
          this.operationResult = OperationResult.fromMatchboxError(error.message);
        }
      });
    this.update = false;
  }

  getPackageUrl(ig: fhir.r4.ImplementationGuide): string {
    return this.fhirPathService.evaluateToString(
      ig,
      "extension.where(url='http://ahdis.ch/fhir/extension/packageUrl').valueUri"
    );
  }

  selectRow(ig: fhir.r4.ImplementationGuide) {
    this.selection = ig;
    this.addPackageId.setValue(this.selection.packageId);
    this.addUrl.setValue(this.getPackageUrl(ig));
    let version: String = this.selection.version;
    if (version) {
      if (version.endsWith(' (current)')) {
        version = version.substring(0, version.length - 10);
      }
    }
    this.addVersion.setValue(version);
  }

  onSubmit() {
    this.errorMessage = null;
    if (this.addPackageId.invalid || this.addVersion.invalid) {
      this.errorMessage = 'Please provide package name';
      return;
    }

    let igId: String = this.addPackageId.value.trim();
    if (igId.indexOf('#') > 0) {
      igId.substring(0, igId.indexOf('#') - 1);
      this.addVersion.setValue(igId.substring(0, igId.indexOf('#') + 1));
    }
    this.addPackageId.setValue(igId);
    const igVersion = this.addVersion.value.trim();
    this.addVersion.setValue(igVersion);

    this.update = true;

    this.client
      .create({
        resourceType: 'ImplementationGuide',
        body: {
          resourceType: 'ImplementationGuide',
          name: igId,
          version: igVersion,
          packageId: igId,
          url: this.addUrl.value,
        },
        options: {
          headers: {
            Prefer: 'return=OperationOutcome',
          },
        },
      })
      .then((response) => {
        this.errorMessage = 'Created Implementation Guide ' + this.addPackageId.value;
        this.operationResult = OperationResult.fromOperationOutcome(response as fhir.r4.OperationOutcome);
        this.search();
      })
      .catch((error) => {
        this.errorMessage = 'Error creating Implementation Guide ' + this.addPackageId.value;
        if (error.response?.data) {
          this.operationResult = OperationResult.fromOperationOutcome(error.response.data);
        } else {
          this.operationResult = OperationResult.fromMatchboxError(error.message);
        }
        this.update = false;
      });
  }

  onUpdate() {
    this.errorMessage = null;

    this.selection.name = this.addPackageId.value;
    this.selection.version = this.addVersion.value;
    this.selection.packageId = this.addPackageId.value;
    this.selection.url = this.addUrl.value;
    this.update = true;

    this.client
      .update({
        resourceType: this.selection.resourceType,
        id: this.selection.id,
        body: this.selection,
        options: {
          headers: {
            Prefer: 'return=OperationOutcome',
          },
        },
      })
      .then((response) => {
        this.errorMessage = 'Updated Implementation Guide ' + this.selection.packageId;
        this.operationResult = OperationResult.fromOperationOutcome(response as fhir.r4.OperationOutcome);
        this.search();
      })
      .catch((error) => {
        this.errorMessage = 'Error updating Implementation Guide ' + this.selection.packageId;
        if (error.response?.data) {
          this.operationResult = OperationResult.fromOperationOutcome(error.response.data);
        } else {
          this.operationResult = OperationResult.fromMatchboxError(error.message);
        }
        this.update = false;
      });
  }

  onDelete() {
    this.errorMessage = null;
    this.update = true;

    this.client
      .delete({
        resourceType: this.selection.resourceType,
        id: this.selection.id,
        options: {
          headers: {
            Prefer: 'return=OperationOutcome',
            'X-Cascade': 'delete',
          },
        },
      })
      .then((response) => {
        this.errorMessage = 'Deleted Implementation Guide Resource ' + this.selection.packageId;
        this.operationResult = OperationResult.fromOperationOutcome(response as fhir.r4.OperationOutcome);
        this.search();
      })
      .catch((error) => {
        this.errorMessage = 'Error deleting Implementation Guide ' + this.selection.packageId;
        if (error.response?.data) {
          this.operationResult = OperationResult.fromOperationOutcome(error.response.data);
        } else {
          this.operationResult = OperationResult.fromMatchboxError(error.message);
        }
        this.update = false;
      });
  }
}
