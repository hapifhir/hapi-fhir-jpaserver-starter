import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import FhirClient from 'fhir-kit-client';
import { AuthConfig } from 'angular-oauth2-oidc';

@Injectable({
  providedIn: 'root',
})
export class FhirConfigService {
  constructor() {}

  public changeFhirMicroService(server: string) {
    localStorage.setItem('fhirMicroServer', server);
  }

  public changeMagMicroService(server: string) {
    localStorage.setItem('magMicroService', server);
  }

  getFhirMicroService(): string {
    const service = localStorage.getItem('fhirMicroServer');
    return service ? service : '/matchboxv3/fhir';
  }

  getMobileAccessGatewayService(): string {
    const service = localStorage.getItem('magMicroService');
    return service ? service : '/mag/fhir';
  }

  getMobileAccessGatewayLoginUrl(): string {
    const service = localStorage.getItem('magMicroService');
    return service.replace('/fhir', '/camel/authorize');
  }

  getMobileAccessGatewayTokenEndpoint(): string {
    const service = localStorage.getItem('magMicroService');
    return service.replace('/fhir', '/camel/token');
  }

  getMobileAccessGatewayAssertionEndpoint(): string {
    const service = localStorage.getItem('magMicroService');
    return service.replace('/fhir', '/camel/assertion');
  }

  getRedirectUri(): string {
    return location.origin + location.pathname + '#/mag';
  }

  getClientId(): string {
    if (this.getRedirectUri().indexOf('localhost') >= 0) {
      return 'matchboxdev';
    }
    return 'matchbox';
  }

  getClientSecret() {
    return 'cd8455fc-e294-465a-8c86-35ae468c6b2f';
  }

  getFhirClient() {
    return new FhirClient({ baseUrl: this.getFhirMicroService() });
  }

  getMobileAccessGatewayClient() {
    return new FhirClient({ baseUrl: this.getMobileAccessGatewayService() });
  }

  getAuthCodeFlowConfig(): AuthConfig {
    return {
      // Url of the Identity Provider

      loginUrl: this.getMobileAccessGatewayLoginUrl(),
      // URL of the SPA to redirect the user to after login
      // redirectUri: window.location.origin + '/index.html',
      redirectUri: this.getRedirectUri(),

      tokenEndpoint: this.getMobileAccessGatewayTokenEndpoint(),

      // The SPA's id. The SPA is registerd with this id at the auth-server
      // clientId: 'server.code',
      clientId: this.getClientId(),
      responseType: 'code',

      // set the scope for the permissions the client should request
      // The first four are defined by OIDC.
      // Important: Request offline_access to get a refresh token
      // The api scope is a usecase specific one
      scope: 'todo',

      dummyClientSecret: this.getClientSecret(),

      showDebugInformation: true,

      // Refresh token after 75% of its live time
      timeoutFactor: 0.75,
    };
  }
}
