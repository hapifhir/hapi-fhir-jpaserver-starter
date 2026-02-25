import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ReplaySubject } from 'rxjs';
import StructureMap = fhir.r4.StructureMap;
import OperationOutcome = fhir.r4.OperationOutcome;
import Bundle = fhir.r4.Bundle;
import { UploadComponent } from '../upload/upload.component';
import { ToastrService } from 'ngx-toastr';
import { parseFhirResource } from '../util/fhir-resource-parser';
import { UploadedFile } from '../upload/uploaded-file';

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

  @ViewChild('resourceUploader') resourceUploader: UploadComponent;
  @ViewChild('mapUploader') mapUploader: UploadComponent;
  @ViewChild('modelUploader') modelUploader: UploadComponent;

  // The content of the resource to transform
  resource: ResourceSource | null = null;

  // The map to use (either its canonical or content)
  map: MapSource | null = null;

  // The provided model(s) to use if necessary (either a StructureDefinition or a Bundle of StructureDefinitions)
  model: string | null = null;

  // The FHIR API client
  client: FhirClient;

  public transformed: any;
  operationOutcome: OperationOutcome;
  operationOutcomeTransformed: OperationOutcome;

  constructor(
    readonly data: FhirConfigService,
    private readonly toastr: ToastrService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
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
            valueString: this.resource.content,
          },
        ],
      };
      if ('content' in this.map) {
        payload.parameter.push({
          name: 'map',
          valueString: this.map.content,
        });
      } else {
        payload.parameter.push({
          name: 'source',
          valueString: this.map.canonical,
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
    const content = await droppedBlob.blob.text();
    const parsed = parseFhirResource(droppedBlob.name, content);
    if (!parsed) {
      this.showErrorToast('Invalid File', 'The uploaded file does not contain a valid FHIR resource.');
      this.resourceUploader.clear();
      this.resource = null;
      return;
    }
    this.resource = {
      content,
      resourceType: parsed.resourceType,
      resourceId: parsed.id,
    };
    this.transformed = null;
  }

  async setMapContent(droppedBlob: UploadedFile) {
    const fileContent = await droppedBlob.blob.text();
    const parsed = parseFhirResource(droppedBlob.name, fileContent);
    if (!parsed) {
      this.showErrorToast('Invalid File', 'The uploaded file does not contain a valid FHIR resource.');
      this.clearMapSelection();
      return;
    }
    if (parsed.resourceType !== 'StructureMap') {
      this.showErrorToast('Invalid Map', 'The uploaded file does not contain a valid StructureMap resource.');
      this.clearMapSelection();
      return;
    }
    if (!parsed.url) {
      this.showErrorToast('Invalid Map', 'The uploaded StructureMap resource does not have a url/canonical.');
      this.clearMapSelection();
      return;
    }
    this.map = {
      content: fileContent,
      canonical: parsed.url,
    };
  }

  async setModelContent(droppedBlob: UploadedFile) {
    const fileContent = await droppedBlob.blob.text();
    if (!parseFhirResource(droppedBlob.name, fileContent)) {
      this.showErrorToast('Invalid Model', 'The uploaded file does not contain a valid FHIR resource.');
      this.modelUploader.clear();
      this.model = null;
      return;
    }
    this.model = fileContent;
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
   * Clears any map selection.
   */
  clearMapSelection() {
    this.map = null;
    this.structureMapControl.setValue(null);
    this.mapUploader.clear();
    this.changeDetectorRef.markForCheck();
  }

  private showErrorToast(title: string, message: string) {
    this.toastr.error(message, title, {
      closeButton: true,
      timeOut: 5000,
    });
  }
}

type MapSource = {
  canonical: string;
  content?: string;
};

type ResourceSource = {
  content: string;
  resourceType: string;
  resourceId?: string;
}
