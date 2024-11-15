import { Component, OnInit } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { IDroppedBlob } from '../upload/upload.component';
import { ReplaySubject } from 'rxjs';
import StructureMap = fhir.r4.StructureMap;
import OperationOutcome = fhir.r4.OperationOutcome;
import Bundle = fhir.r4.Bundle;

@Component({
  selector: 'app-transform',
  templateUrl: './transform.component.html',
  styleUrls: ['./transform.component.scss'],
})
export class TransformComponent implements OnInit {
  // The list of all structure maps, as advertised by the server
  allStructureMaps: StructureMap[];

  // The list of filtered structure maps, to display in the select box
  public filteredStructureMaps: ReplaySubject<StructureMap[]> = new ReplaySubject<StructureMap[]>(1);

  // The form control for the structure map filter
  public structureMapFilterControl: FormControl<string> = new FormControl<string>('');

  // The form control for the selected structure map
  public structureMapControl: FormControl<string> = new FormControl<string>(null);

  selectedUrl: string;

  client: FhirClient;

  source: string;
  mimeType: string;

  public transformed: any;
  operationOutcome: OperationOutcome;
  operationOutcomeTransformed: OperationOutcome;

  constructor(data: FhirConfigService) {
    this.client = data.getFhirClient();
    this.client
      .search({
        resourceType: 'StructureMap',
        searchParams: {
          _summary: 'true',
          _sort: 'name',
        },
      })
      .then((response) => {
        this.setMaps(<Bundle>response);
        this.filteredStructureMaps.next(this.allStructureMaps.slice());
      });

    this.structureMapControl.valueChanges.pipe(debounceTime(400), distinctUntilChanged()).subscribe((term) => {
      this.selectedUrl = term;
      this.transform();
    });

    // Listen for changes in the filter text
    this.structureMapFilterControl.valueChanges.subscribe(() => this.filterStructureMaps());
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

  setMaps(response: Bundle) {
    this.allStructureMaps = response.entry.map((entry) => <StructureMap>entry.resource);
  }

  ngOnInit(): void {}

  addFile(droppedBlob: IDroppedBlob) {
    this.transformed = null;
    if (droppedBlob.contentType === 'application/json' || droppedBlob.name.endsWith('.json')) {
      this.mimeType = 'application/fhir+json';
    }
    if (droppedBlob.contentType === 'application/xml' || droppedBlob.name.endsWith('.xml')) {
      this.mimeType = 'application/fhir+xml';
    }
    const reader = new FileReader();
    reader.readAsText(droppedBlob.blob);
    reader.onload = () => {
      this.source = <string>reader.result;
    };
  }

  protected filterStructureMaps() {
    if (!this.allStructureMaps) {
      return;
    }
    // get the search keyword
    let search = this.structureMapFilterControl.value;
    if (!search) {
      this.filteredStructureMaps.next(this.allStructureMaps.slice());
      return;
    }
    search = search.toLowerCase();
    this.filteredStructureMaps.next(
      this.allStructureMaps.filter((structureMap) => structureMap.name.toLowerCase().indexOf(search) > -1)
    );
  }
}
