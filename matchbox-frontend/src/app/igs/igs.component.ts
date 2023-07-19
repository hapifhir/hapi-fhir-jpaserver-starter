import { Component, OnInit } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import { PageEvent } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { UntypedFormControl, Validators } from '@angular/forms';
import FhirClient from 'fhir-kit-client';
import debug from 'debug';
import { FhirPathService } from '../fhirpath.service';

@Component({
  selector: 'app-igs',
  templateUrl: './igs.component.html',
  styleUrls: ['./igs.component.scss'],
})
export class IgsComponent implements OnInit {
  public addPackageId: UntypedFormControl;
  public addVersion: UntypedFormControl;
  public addUrl: UntypedFormControl;
  public selection: fhir.r4.ImplementationGuide;

  length = 100;
  pageSize = 1000;
  pageIndex = 0;
  pageSizeOptions = [this.pageSize];

  bundle: fhir.r4.Bundle;
  dataSource = new MatTableDataSource<fhir.r4.BundleEntry>();

  update: boolean = false;

  client: FhirClient;
  static log = debug('app:');
  errMsg: string;
  operationOutcome: fhir.r4.OperationOutcome;

  query = {
    _sort: 'title',
    _count: this.pageSize,
  };

  constructor(private data: FhirConfigService, private fhirPathService: FhirPathService) {
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
        this.pageIndex = 0;
        this.setBundle(<fhir.r4.Bundle>response);
        this.selection = undefined;
        this.addPackageId.setValue('');
        this.addVersion.setValue('');
        this.addUrl.setValue('');
      })
      .catch((error) => {
        this.errMsg = 'Error accessing FHIR server';
        this.operationOutcome = error.response.data;
      });
    this.update = false;
  }

  getPackageId(entry: fhir.r4.BundleEntry): string {
    const ig = <fhir.r4.ImplementationGuide>entry.resource;
    if (ig.packageId) {
      return ig.packageId;
    }
    return '';
  }

  getTitle(entry: fhir.r4.BundleEntry): string {
    const ig = <fhir.r4.ImplementationGuide>entry.resource;
    if (ig.title) {
      return ig.title;
    }
    return '';
  }

  getVersion(entry: fhir.r4.BundleEntry): string {
    const ig = <fhir.r4.ImplementationGuide>entry.resource;
    if (ig.version) {
      return ig.version;
    }
    return '';
  }

  getPackageUrl(entry: fhir.r4.BundleEntry): string {
    const ig = <fhir.r4.ImplementationGuide>entry.resource;
    return this.fhirPathService.evaluateToString(
      ig,
      "extension.where(url='http://ahdis.ch/fhir/extension/packageUrl').valueUri"
    );
  }

  setBundle(bundle: fhir.r4.Bundle) {
    this.bundle = <fhir.r4.Bundle>bundle;
    this.dataSource.data = this.bundle.entry;
    this.length = this.bundle.total;
    this.selection = undefined;
  }

  ngOnInit() {}

  selectRow(row: fhir.r4.BundleEntry) {
    this.selection = row.resource as fhir.r4.ImplementationGuide;
    this.addPackageId.setValue(this.selection.packageId);
    this.addUrl.setValue(this.getPackageUrl(row));
    let version: String = this.selection.version;
    if (version) {
      if (version.endsWith(' (current)')) {
        version = version.substring(0, version.length - 10);
      }
    }
    this.addVersion.setValue(version);
  }

  onSubmit() {
    IgsComponent.log('onSubmit ' + this.addPackageId.value);

    this.errMsg = null;

    if (this.addPackageId.invalid || this.addVersion.invalid) {
      this.errMsg = 'Please provide package name';
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
        this.errMsg = 'Created Implementation Guide ' + this.addPackageId.value;
        this.operationOutcome = response as fhir.r4.OperationOutcome;
        this.search();
      })
      .catch((error) => {
        this.errMsg = 'Error creating Implementation Guide ' + this.addPackageId.value;
        this.operationOutcome = error.response.data;
        this.update = false;
      });
  }

  goToPage(event: PageEvent) {
    if (event.pageIndex > this.pageIndex) {
      this.client.nextPage({ bundle: this.bundle }).then((response) => {
        this.pageIndex = event.pageIndex;
        this.setBundle(<fhir.r4.Bundle>response);
        this.selection = undefined;
        console.log('next page called ');
      });
    } else {
      this.client.prevPage({ bundle: this.bundle }).then((response) => {
        this.pageIndex = event.pageIndex;
        this.setBundle(<fhir.r4.Bundle>response);
        this.selection = undefined;
        console.log('previous page called ');
      });
    }
  }

  // Prefer: return=OperationOutcome

  onUpdate() {
    this.errMsg = null;

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
        this.errMsg = 'Updated Implementation Guide ' + this.selection.packageId;
        this.operationOutcome = response as fhir.r4.OperationOutcome;
        this.search();
      })
      .catch((error) => {
        this.errMsg = 'Error updating Implementation Guide ' + this.selection.packageId;
        this.operationOutcome = error.response.data;
        this.update = false;
      });
  }

  onDelete() {
    this.errMsg = null;
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
        this.errMsg = 'Deleted Implementation Guide Resource ' + this.selection.packageId;
        this.operationOutcome = response as fhir.r4.OperationOutcome;
        this.search();
      })
      .catch((error) => {
        this.errMsg = 'Error deleting Implementation Guide ' + this.selection.packageId;
        this.operationOutcome = error.response.data;
        this.update = false;
      });
  }
}
