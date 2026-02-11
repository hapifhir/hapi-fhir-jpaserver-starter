// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

// The URL to connect to the localhost:8080 instance.
const LOCALHOST_PROXY_URL = 'http://localhost:4200/proxy/localhost';

// The URL to connect to the test.ahdis.ch/matchboxv3 instance.
const MATCHBOXV3_PROXY_URL = 'http://localhost:4200/proxy/testahdisch';

export const environment = {
  production: false,
  fhirServerUrl: () => MATCHBOXV3_PROXY_URL,
};
