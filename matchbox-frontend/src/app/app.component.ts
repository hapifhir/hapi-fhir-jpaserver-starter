import { Component } from '@angular/core';
import { FhirConfigService } from './fhirConfig.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  constructor(
    translateService: TranslateService,
    fhirConfigService: FhirConfigService
  ) {
    translateService.setDefaultLang('de');
    translateService.use(translateService.getBrowserLang());

    let base = location.origin; 
    if (base === 'http://localhost:4200') {
      console.log('note: using local dev mag system for' + location.origin);
      fhirConfigService.changeFhirMicroService(
        'http://localhost:8080/matchbox/fhir'
      );
    } else {
      let url:string = base+location.pathname+"fhir";
      fhirConfigService.changeFhirMicroService(url);
      console.log('fhir endpoint ' + url);
    }
  }
}
