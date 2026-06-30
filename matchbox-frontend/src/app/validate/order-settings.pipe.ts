import { Pipe, PipeTransform } from "@angular/core";
import {ValidationParameterDefinition} from "./validation-parameter";

@Pipe({
  name: "sortSettings",
  standalone: true
})
export class SortSettingsPipe implements PipeTransform {
  transform(definitions: ValidationParameterDefinition[], ...args: unknown[]): ValidationParameterDefinition[] {
    definitions.sort((d1: ValidationParameterDefinition, d2: ValidationParameterDefinition): number => {
      if (d1.param.type == d2.param.type) {
        return d1.param.name.localeCompare(d2.param.name);
      }
      if (d1.param.type && d2.param.type) {
        return d1.param.type.localeCompare(d2.param.type);
      }
      return 0;
    });
    //console.log(definitions.map(d => `${d.param.type}  ${d.param.name}`));
    return definitions;
  }
}
