import { Component } from '@angular/core';
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
export class TransformComponent {
  // The list of all structure maps, as advertised by the server
  allStructureMaps: StructureMap[] = [];

  // The list of filtered structure maps, to display in the select box
  public filteredStructureMaps: ReplaySubject<StructureMap[]> = new ReplaySubject<StructureMap[]>(1);

  // The form control for the structure map filter
  public structureMapFilterControl: FormControl<string> = new FormControl<string>('');

  // The form control for the selected structure map
  public structureMapControl: FormControl<string> = new FormControl<string>(null);

  // The content of the resource to transform
  resource: string;

  // The map to use (either its canonical or content)
  map: MapSource | null = null;

  // The provided model(s) to use if necessary (either a StructureDefinition or a Bundle of StructureDefinitions)
  model: string | null = null;

  // The FHIR API client
  client: FhirClient;

  mimeType: string;

  public transformed: any;
  operationOutcome: OperationOutcome;
  operationOutcomeTransformed: OperationOutcome;

  constructor(data: FhirConfigService) {
    this.client = data.getFhirClient();
    this.client
      .operation({
        name: 'list',
        resourceType: 'StructureMap',
        method: 'GET',
      })
      .then((response: Bundle) => {
        this.setMaps(response);
        this.filteredStructureMaps.next(this.allStructureMaps.slice());
      });

    this.structureMapControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((url) => (this.map = { canonical: url }));

    // Listen for changes in the filter text
    this.structureMapFilterControl.valueChanges.subscribe(() => this.filterStructureMaps());
  }

  transform() {
    if (this.resource != null && this.map != null) {
      const payload = {
        resourceType: 'Parameters',
        parameter: [
          {
            name: 'resource',
            valueString: this.resource
          },
        ],
      };
      if ('canonical' in this.map) {
        payload.parameter.push({
          'name': 'source',
          'valueString': this.map.canonical,
        });
      } else {
        payload.parameter.push({
          'name': 'map',
          'valueString': this.map.content,
        });
      }
      if (this.model != null) {
        payload.parameter.push({
          'name': 'model',
          'valueString': this.model,
        });
      }
      console.log(payload);

      this.client
        .operation({
          name: 'transform',
          resourceType: 'StructureMap',
          input: payload,
          options: {
            headers: {
              'content-type': 'application/fhir+json',
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

  getResource(): String {
    return this.resource;
  }

  getMapped(): String {
    return JSON.stringify(this.transformed, null, 2);
  }

  setMaps(response: Bundle) {
    this.allStructureMaps = response.entry.map((entry) => <StructureMap>entry.resource);
  }

  setResource(droppedBlob: IDroppedBlob) {
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
      this.resource = <string>reader.result;
    };
  }

  setMapContent(droppedBlob: IDroppedBlob) {
    const reader = new FileReader();
    reader.readAsText(droppedBlob.blob);
    reader.onload = () => {
      this.map = {content: <string>reader.result};
    };
  }

  setModelContent(droppedBlob: IDroppedBlob) {
    const reader = new FileReader();
    reader.readAsText(droppedBlob.blob);
    reader.onload = () => {
      this.model = <string>reader.result;
    };
  }

  protected filterStructureMaps() {
    console.log('filtering');
    if (!this.allStructureMaps || this.allStructureMaps.length === 0) {
      console.log('return early');
      return;
    }
    // get the search keyword
    let search = this.structureMapFilterControl.value;
    if (!search) {
      this.filteredStructureMaps.next(this.allStructureMaps.slice());
      console.log('no search');
      return;
    }
    search = search.toLowerCase();
    this.filteredStructureMaps.next(
      this.allStructureMaps.filter((structureMap) => structureMap.name.toLowerCase().indexOf(search) > -1)
    );
  }
}

type MapCanonical = {
  canonical: string;
  content?: never;
};

type MapContent = {
  canonical?: never;
  content: string;
};

type MapSource = NonNullable<MapCanonical | MapContent>;
