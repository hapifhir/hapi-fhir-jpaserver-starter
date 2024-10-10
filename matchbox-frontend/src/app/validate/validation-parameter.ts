import {UntypedFormControl} from "@angular/forms";

export class ValidationParameter {
  param: fhir.r4.OperationDefinitionParameter;
  formControl: UntypedFormControl;

  constructor(param: fhir.r4.OperationDefinitionParameter) {
    this.param = param;
    this.formControl = new UntypedFormControl();
  }
}
