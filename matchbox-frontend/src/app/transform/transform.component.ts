import { Component } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ReplaySubject } from 'rxjs';
import StructureMap = fhir.r4.StructureMap;
import OperationOutcome = fhir.r4.OperationOutcome;
import Bundle = fhir.r4.Bundle;
import { MatTabChangeEvent } from '@angular/material/tabs';
import { UploadComponent } from '../upload/upload.component';

@Component({
  selector: 'app-transform',
  templateUrl: './transform.component.html',
  styleUrls: ['./transform.component.scss'],
  standalone: false,
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

  public mapUploader: UploadComponent;

  // The content of the resource to transform
  resource: string;

  // The map to use (either its canonical or content)
  map: MapSource | null = null;

  // The provided model(s) to use if necessary (either a StructureDefinition or a Bundle of StructureDefinitions)
  model: string | null = null;

  // The FHIR API client
  client: FhirClient;

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

    this.structureMapControl.valueChanges.pipe(debounceTime(400), distinctUntilChanged()).subscribe((url) => {
      this.map = { canonical: url };
    });

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
            valueString: this.resource,
          },
        ],
      };
      if ('canonical' in this.map) {
        payload.parameter.push({
          name: 'source',
          valueString: this.map.canonical,
        });
      } else {
        payload.parameter.push({
          name: 'map',
          valueString: this.map.content,
        });
      }
      if (this.model != null) {
        payload.parameter.push({
          name: 'model',
          valueString: this.model,
        });
      }

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

  getMapped(): string {
    return JSON.stringify(this.transformed, null, 2);
  }

  setMaps(response: Bundle) {
    this.allStructureMaps = response.entry.map((entry) => <StructureMap>entry.resource);
  }

  async setResource(droppedBlob: UploadedFile) {
    this.resource = await droppedBlob.blob.text();
    this.transformed = null;
  }

  async setMapContent(droppedBlob: UploadedFile) {
    this.map = { content: await droppedBlob.blob.text() };
  }

  async setModelContent(droppedBlob: UploadedFile) {
    this.model = await droppedBlob.blob.text();
  }

  protected filterStructureMaps() {
    if (!this.allStructureMaps || this.allStructureMaps.length === 0) {
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
      this.allStructureMaps.filter((structureMap) => structureMap.title.toLowerCase().indexOf(search) > -1)
    );
  }

  /**
   * Fired when the user changes the map selection tab.
   * Clears the map selection and content to avoid confusion between the two modes.
   */
  mapTabChanged() {
    this.map = null;
    this.structureMapControl.setValue(null);
    this.mapUploader.clear();
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
