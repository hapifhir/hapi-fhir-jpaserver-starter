import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FhirPathService } from '../fhirpath.service';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-fhir-path',
  templateUrl: './fhir-path.component.html',
  styleUrls: ['./fhir-path.component.scss'],
})
export class FhirPathComponent implements OnInit {
  public fhirPathExpression: FormControl;
  public fhirPathResult: any;
  public resource: any;

  constructor(
    private fhirPathService: FhirPathService,
    private cd: ChangeDetectorRef
  ) {
    this.fhirPathExpression = new FormControl();
    this.fhirPathExpression.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((term) => {
        console.log('called with ' + term);
        if (term) {
          try {
            this.fhirPathResult = this.fhirPathService.evaluate(
              this.resource,
              term
            );
          } catch (e) {
            console.log(e);
            this.fhirPathResult = undefined;
          }
        } else {
          this.fhirPathResult = undefined;
        }
      });
  }

  fileChange(event) {
    const reader = new FileReader();

    if (event.target.files && event.target.files.length) {
      const [file] = event.target.files;
      reader.readAsText(file);
      reader.onload = () => {
        this.resource = JSON.parse(<string>reader.result);
        // need to run CD since file load runs outside of zone
        this.cd.markForCheck();
      };
    }
  }

  getFhirPathResult(): string {
    return JSON.stringify(this.fhirPathResult, null, 2);
  }

  getJson(): string {
    return JSON.stringify(this.resource, null, 2);
  }

  ngOnInit() {}
}
