import { Injectable } from '@angular/core';
import FhirClient from 'fhir-kit-client';

@Injectable({
  providedIn: 'root',
})
export class FhirConfigService {
  constructor() {}

  public changeFhirMicroService(server: string) {
    localStorage.setItem('fhirMicroServer', server);
  }

  getFhirMicroService(): string {
    return localStorage.getItem('fhirMicroServer');
  }

  getFhirClient() {
    return new FhirClient({ baseUrl: this.getFhirMicroService() });
  }
}
