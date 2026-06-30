import { Component, ChangeDetectionStrategy } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import { UntypedFormControl, Validators } from '@angular/forms';
import { FhirPathService } from '../fhirpath.service';
import { OperationResult } from '../util/operation-result';
import { FhirClientWrapper } from '../util/fhir-client-wrapper';
import Bundle = fhir.r4.Bundle;

@Component({
  selector: 'app-igs',
  templateUrl: './igs.component.html',
  styleUrls: ['./igs.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
})
export class IgsComponent {
  public addPackageId: UntypedFormControl;
  public addVersion: UntypedFormControl;
  public addUrl: UntypedFormControl;
  public selection: fhir.r4.ImplementationGuide | null = null;

  update: boolean = false;

  client: FhirClientWrapper;
  igs: fhir.r4.ImplementationGuide[] = [];
  errorMessage: string | null = null;
  operationResult: OperationResult | null = null;

  pageSize = 20;
  currentOffset = 0;
  totalCount: number | null = null;

  query = {
    _sort: 'title',
    _count: 20,
    _offset: 0,
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
    this.query._offset = this.currentOffset;
    this.query._count = this.pageSize;
    this.client
      .search({ resourceType: 'ImplementationGuide', searchParams: this.query })
      .then((bundle: Bundle) => {
        this.igs = bundle.entry ? bundle.entry.map((entry) => <fhir.r4.ImplementationGuide>entry.resource) : [];
        this.totalCount = bundle.total ?? null;
        this.selection = null;
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

  get hasPreviousPage(): boolean {
    return this.currentOffset > 0;
  }

  get hasNextPage(): boolean {
    return this.totalCount !== null && this.currentOffset + this.pageSize < this.totalCount;
  }

  get currentPage(): number {
    return Math.floor(this.currentOffset / this.pageSize) + 1;
  }

  get totalPages(): number {
    if (this.totalCount === null) return 0;
    return Math.ceil(this.totalCount / this.pageSize);
  }

  previousPage() {
    this.currentOffset = Math.max(0, this.currentOffset - this.pageSize);
    this.search();
  }

  nextPage() {
    this.currentOffset += this.pageSize;
    this.search();
  }

  getPackageUrl(ig: fhir.r4.ImplementationGuide): string {
    return this.fhirPathService.evaluateToString(
      ig,
      "extension.where(url='http://ahdis.ch/fhir/extension/packageUrl').valueUri"
    );
  }

  isCurrent(ig: fhir.r4.ImplementationGuide): boolean {
    let result = this.fhirPathService.evaluateToString(
      ig,
      "meta.tag.where(system='http://matchbox.health/fhir/CodeSystem/tag').code"
    );
    return result === 'current';
  }

  selectRow(ig: fhir.r4.ImplementationGuide) {
    this.selection = ig;
    this.addPackageId.setValue(this.selection.packageId);
    this.addUrl.setValue(this.getPackageUrl(ig));
    let version = this.selection.version;
    if (version) {
      if (version.endsWith(' (last)')) {
        version = version.substring(0, version.length - 7);
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
        this.operationResult = OperationResult.fromOperationOutcome(response);
        this.currentOffset = 0;
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
    if (!this.selection) {
      this.errorMessage = 'No Implementation Guide selected';
      return;
    }

    this.selection.name = this.addPackageId.value;
    this.selection.version = this.addVersion.value;
    this.selection.packageId = this.addPackageId.value;
    this.selection.url = this.addUrl.value;
    this.update = true;

    const selection = this.selection;
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
        this.errorMessage = 'Updated Implementation Guide ' + selection.packageId;
        this.operationResult = OperationResult.fromOperationOutcome(response);
        this.currentOffset = 0;
        this.search();
      })
      .catch((error) => {
        this.errorMessage = 'Error updating Implementation Guide ' + selection.packageId;
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
    if (!this.selection) {
      this.errorMessage = 'No Implementation Guide selected';
      return;
    }

    const selection = this.selection;
    this.client
      .delete({
        resourceType: this.selection.resourceType,
        id: this.selection.id ?? '',
        options: {
          headers: {
            Prefer: 'return=OperationOutcome',
            'X-Cascade': 'delete',
          },
        },
      })
      .then((response) => {
        this.errorMessage = 'Deleted Implementation Guide Resource ' + selection.packageId;
        this.operationResult = OperationResult.fromOperationOutcome(response);
        this.currentOffset = 0;
        this.search();
      })
      .catch((error) => {
        this.errorMessage = 'Error deleting Implementation Guide ' + selection.packageId;
        if (error.response?.data) {
          this.operationResult = OperationResult.fromOperationOutcome(error.response.data);
        } else {
          this.operationResult = OperationResult.fromMatchboxError(error.message);
        }
        this.update = false;
      });
  }
}
