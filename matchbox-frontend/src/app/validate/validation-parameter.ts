import {UntypedFormControl} from "@angular/forms";

export class ValidationParameterDefinition {
  param: fhir.r4.OperationDefinitionParameter;
  formControl: UntypedFormControl;

  constructor(param: fhir.r4.OperationDefinitionParameter) {
    this.param = param;
    this.formControl = new UntypedFormControl();
  }
}

export class ValidationParameter {
  constructor(readonly name: string, readonly value: string) {
  }
}
