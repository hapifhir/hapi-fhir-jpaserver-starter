export const environment = {
  production: true,
  fhirServerUrl: () => (window as any).MATCHBOX_BASE_PATH + '/fhir',
};
