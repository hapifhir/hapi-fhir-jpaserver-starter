import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import FhirClient from 'fhir-kit-client';
import debug from 'debug';
import { ThrowStmt } from '@angular/compiler';

@Component({
  selector: 'app-mapping-language',
  templateUrl: './mapping-language.component.html',
  styleUrls: ['./mapping-language.component.scss'],
})
export class MappingLanguageComponent implements OnInit {
  public source: FormControl;
  public map: FormControl;
  public structureMap: any;
  public transformed: any;

  client: FhirClient;
  static log = debug('app:');
  errMsg: string;

  operationOutcome: fhir.r4.OperationOutcome;
  operationOutcomeTransformed: fhir.r4.OperationOutcome;

  constructor(private cd: ChangeDetectorRef, private data: FhirConfigService) {
    this.client = data.getFhirClient();
    this.source = new FormControl();
    this.map = new FormControl();
    this.structureMap = null;
    this.map.valueChanges
      .pipe(debounceTime(1000), distinctUntilChanged())
      .subscribe((mapText) => {
        MappingLanguageComponent.log('create StructureMap');
        this.client
          .create({
            resourceType: 'StructureMap',
            body: mapText,
            headers: {
              accept: 'application/fhir+json',
              'content-type': 'text/fhir-mapping',
            },
          })
          .then((response) => {
            this.operationOutcome = null;
            this.structureMap = response;
            this.transform();
          })
          .catch((error) => {
            // {"response":{"status":500,"data":{"resourceType":"OperationOutcome","issue":[{"severity":"error","code":"processing","diagnostics":"Error @1, 1: Found \"asdfasdf\" expecting \"map\""}]}},"config":{"method":"post","url":"https://test.ahdis.ch/r4/StructureMap","headers":{}}}
            this.structureMap = null;
            this.operationOutcome = error.response.data;
          });
      });

    this.source.valueChanges
      .pipe(debounceTime(1000), distinctUntilChanged())
      .subscribe((sourceText) => this.transform());
  }

  transform() {
    MappingLanguageComponent.log('transform Source');
    let res: fhir.r4.Resource = JSON.parse(this.source.value);
    if (this.structureMap != null) {
      this.client
        .operation({
          name: 'transform?source=' + encodeURIComponent(this.structureMap.url),
          resourceType: 'StructureMap',
          input: res,
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

  ngOnInit() {}

  fileSource(event) {
    const reader = new FileReader();

    if (event.target.files && event.target.files.length) {
      const [file] = event.target.files;
      reader.readAsText(file);
      reader.onload = () => {
        this.source.setValue(reader.result);
        // need to run CD since file load runs outside of zone
        this.cd.markForCheck();
      };
    }
  }

  fileChange(event) {
    const reader = new FileReader();

    if (event.target.files && event.target.files.length) {
      const [file] = event.target.files;
      reader.readAsText(file);
      reader.onload = () => {
        this.map.setValue(reader.result);
        // need to run CD since file load runs outside of zone
        this.cd.markForCheck();
      };
    }
  }
}
