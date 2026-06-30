import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';
import { FhirClientWrapper } from './util/fhir-client-wrapper';

@Injectable({
  providedIn: 'root',
})
export class FhirConfigService {
  constructor() {}

  public changeFhirMicroService(server: string) {
    localStorage.setItem('fhirMicroServer', server);
  }

  getFhirMicroService(): string {
    return localStorage.getItem('fhirMicroServer') ?? environment.fhirServerUrl();
  }

  getFhirClient(): FhirClientWrapper {
    return new FhirClientWrapper(this.getFhirMicroService());
  }
}
