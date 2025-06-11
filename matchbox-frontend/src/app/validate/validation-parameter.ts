import {UntypedFormControl} from "@angular/forms";

export class ValidationParameterDefinition {
  param: fhir.r4.OperationDefinitionParameter;
  formControl: UntypedFormControl;

  constructor(param: fhir.r4.OperationDefinitionParameter) {
    this.param = param;
    this.formControl = new UntypedFormControl();
    if (this.param.extension) {
      if (this.param.type == "boolean") {
        this.formControl.setValue(param.extension[0].valueBoolean);
      } else {
        // Concatenate all extension values if it's a string parameter
        if (this.param.max === '*') {
          // For multi-line parameters (textarea), join with newlines
          const values = param.extension
            .filter(ext => ext.valueString !== undefined)
            .map(ext => ext.valueString);
          this.formControl.setValue(values.join('\n'));
        } else {
          this.formControl.setValue(param.extension[0].valueString);
        }
      }
    }
  }
}

export class ValidationParameter {
  constructor(readonly name: string, readonly value: string) {
  }
}
