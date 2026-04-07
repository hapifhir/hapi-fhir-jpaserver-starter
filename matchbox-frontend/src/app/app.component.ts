import { Component } from '@angular/core';
import { FhirConfigService } from './fhirConfig.service';
import { TranslateService } from '@ngx-translate/core';
import {HashUrlRedirectionService} from "./util/hash-url-redirection-service";
import { environment } from '../environments/environment';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
})
export class AppComponent {
  public version: string = (window as any).MATCHBOX_VERSION;
  public saveStatistics: boolean = (window as any).MATCHBOX_SAVE_STATISTICS_ENABLED;

  constructor(readonly translateService: TranslateService,
              readonly fhirConfigService: FhirConfigService,
              readonly hashUrlRedirectionService: HashUrlRedirectionService) {
    // Redirect any old URL with hash to the new URL
    if (hashUrlRedirectionService.isHashUrl()) {
      hashUrlRedirectionService.redirectHashUrl();
    }

    translateService.setFallbackLang('de');
    translateService.use(translateService.getBrowserLang());

    // Setting the FHIR server URL from the environment configuration.
    // This allows us to switch between different FHIR servers (e.g., for development, testing, production) without changing the code.
    const fhirServerUrl = environment.fhirServerUrl();
    console.info(`INFO: connecting to the FHIR server at ${fhirServerUrl}`);
    fhirConfigService.changeFhirMicroService(fhirServerUrl);
  }
}
