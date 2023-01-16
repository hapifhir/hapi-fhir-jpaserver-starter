import { Injectable } from '@angular/core';
import { evaluate } from 'fhirpath';

@Injectable({
  providedIn: 'root',
})
export class FhirPathService {
  public evaluate(fhir: any, fhirPath: string): any {
    return evaluate(fhir, fhirPath, null);
  }

  public evaluateToString(fhir: any, fhirPath: string): string {
    const result = this.evaluate(fhir, fhirPath);
    if (
      result &&
      result instanceof Array &&
      (result as Array<string>).length === 1
    ) {
      return (result as Array<string>)[0];
    }
    return null;
  }

  getOauthUriToken(capabilityStatement: any): string {
    return this.evaluateToString(
      capabilityStatement,
      "rest.security.extension.where(url='http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris').extension.where(url='token').valueUri"
    );
  }

  getOauthUriAuthorize(capabilityStatement: any): string {
    return this.evaluateToString(
      capabilityStatement,
      "rest.security.extension.where(url='http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris').extension.where(url='authorize').valueUri"
    );
  }
}
