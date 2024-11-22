import { Component } from '@angular/core';
import { FhirConfigService } from './fhirConfig.service';
import { TranslateService } from '@ngx-translate/core';
import packageJson from '../../package.json';
import {HashUrlRedirectionService} from "./util/hash-url-redirection-service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
})
export class AppComponent {
  public version: string = packageJson.version;

  constructor(readonly translateService: TranslateService,
              readonly fhirConfigService: FhirConfigService,
              readonly hashUrlRedirectionService: HashUrlRedirectionService) {
    // Redirect any old URL with hash to the new URL
    if (hashUrlRedirectionService.isHashUrl()) {
      hashUrlRedirectionService.redirectHashUrl();
    }

    translateService.setDefaultLang('de');
    translateService.use(translateService.getBrowserLang());

    let base = location.origin;
    if (base === 'http://localhost:4200') {
      console.log('note: using local dev mag system for ' + location.origin);
      // You can also use /proxy/testahdisch
      fhirConfigService.changeFhirMicroService('http://localhost:4200/proxy/localhost/matchboxv3/fhir');
    } else {
      const url: string = (window as any).MATCHBOX_BASE_PATH + '/fhir';
      fhirConfigService.changeFhirMicroService(url);
      console.log('fhir endpoint ' + url);
    }
  }
}
