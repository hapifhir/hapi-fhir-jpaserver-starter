import { Pipe, PipeTransform } from "@angular/core";
import {ValidationParameterDefinition} from "./validation-parameter";

@Pipe({
  name: "sortSettings",
})
export class SortSettingsPipe implements PipeTransform {
  transform(definitions: ValidationParameterDefinition[], ...args: unknown[]): ValidationParameterDefinition[] {
    definitions.sort((d1, d2): number => {
      if (d1.param.type == d2.param.type) {
        return d1.param.name.localeCompare(d2.param.name);
      }
      return d1.param.type.localeCompare(d2.param.type);
    });
    console.log(definitions.map(d => `${d.param.type}  ${d.param.name}`));
    return definitions;
  }
}
