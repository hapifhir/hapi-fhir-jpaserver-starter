import {UntypedFormControl} from "@angular/forms";

export class ValidationParameter {
  param: fhir.r4.OperationDefinitionParameter;
  valueBoolean: boolean;
  valueString;
  String;
  formControl: UntypedFormControl;

  constructor(param: fhir.r4.OperationDefinitionParameter) {
    this.param = param;
    this.formControl = new UntypedFormControl();
  }

  isValueSet(): boolean {
    return this.valueBoolean != null || this.valueString != null;
  }
}
