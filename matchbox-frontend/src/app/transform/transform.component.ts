import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { IDroppedBlob } from '../upload/upload.component';

@Component({
  selector: 'app-transform',
  templateUrl: './transform.component.html',
  styleUrls: ['./transform.component.scss'],
})
export class TransformComponent implements OnInit {
  structureMaps: fhir.r4.StructureMap[];

  selectedUrl: string;

  client: FhirClient;
  maps: Map<String, String>;

  source: string;
  mimeType: string;

  selectedMap: FormControl;

  query = {
    _summary: 'true',
    _sort: 'name',
  };

  panelOpenState = false;

  public transformed: any;
  errMsg: string;
  operationOutcome: fhir.r4.OperationOutcome;
  operationOutcomeTransformed: fhir.r4.OperationOutcome;

  constructor(private data: FhirConfigService, private cd: ChangeDetectorRef) {
    this.client = data.getFhirClient();
    this.client
      .search({ resourceType: 'StructureMap', searchParams: this.query })
      .then((response) => {
        this.setMaps(<fhir.r4.Bundle>response);
        return response;
      });

    this.selectedMap = new FormControl();
    this.selectedMap.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((term) => {
        this.selectedUrl = term;
        this.transform();
      });
  }

  transform() {
    if (this.source != null && this.selectedUrl != null) {
      this.client
        .operation({
          name: 'transform?source=' + encodeURIComponent(this.selectedUrl),
          resourceType: 'StructureMap',
          input: this.source,
          options: {
            headers: {
              'content-type': this.mimeType,
            },
          },
        })
        .then((response) => {
          this.operationOutcomeTransformed = null;
          this.transformed = response;
        })
        .catch((error) => {
          this.transformed = null;
          this.operationOutcomeTransformed = error.response.data;
        });
    }
  }

  getSource(): String {
    return this.source;
  }

  getMapped(): String {
    return JSON.stringify(this.transformed, null, 2);
  }

  setMaps(response: fhir.r4.Bundle) {
    this.structureMaps = response.entry.map(
      (entry) => <fhir.r4.StructureMap>entry.resource
    );
  }
  ngOnInit(): void {}

  addFile(droppedBlob: IDroppedBlob) {
    this.transformed = null;
    if (
      droppedBlob.contentType === 'application/json' ||
      droppedBlob.name.endsWith('.json')
    ) {
      this.mimeType = 'application/fhir+json';
    }
    if (
      droppedBlob.contentType === 'application/xml' ||
      droppedBlob.name.endsWith('.xml')
    ) {
      this.mimeType = 'application/fhir+xml';
    }
    const reader = new FileReader();
    reader.readAsText(droppedBlob.blob);
    reader.onload = () => {
      this.source = <string>reader.result;
    };
  }
}
