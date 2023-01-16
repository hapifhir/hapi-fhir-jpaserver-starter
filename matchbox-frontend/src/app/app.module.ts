import { HttpClient, HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { AppComponent } from './app.component';
import { CapabilityStatementComponent } from './capability-statement/capability-statement.component';
import { FhirPathComponent } from './fhir-path/fhir-path.component';
import { HomeComponent } from './home/home.component';
import { MappingLanguageComponent } from './mapping-language/mapping-language.component';
import { SettingsComponent } from './settings/settings.component';
import { SharedModule } from './shared/shared.module';
import { IgsComponent } from './igs/igs.component';
import { HIGHLIGHT_OPTIONS, HighlightModule } from 'ngx-highlightjs';
import { TransformComponent } from './transform/transform.component';
import { ValidateComponent } from './validate/validate.component';
import { OperationOutcomeComponent } from './operation-outcome/operation-outcome.component';
import { UploadComponent } from './upload/upload.component';
import { OAuthModule } from 'angular-oauth2-oidc';

const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
  },
  {
    path: 'fhirpath',
    component: FhirPathComponent,
  },
  {
    path: 'mappinglanguage',
    component: MappingLanguageComponent,
  },
  {
    path: 'CapabilityStatement',
    component: CapabilityStatementComponent,
  },
  {
    path: 'igs',
    component: IgsComponent,
  },
  {
    path: 'settings',
    component: SettingsComponent,
  },
  {
    path: 'transform',
    component: TransformComponent,
  },
  {
    path: 'validate',
    component: ValidateComponent,
  },
];

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

@NgModule({
  declarations: [
    AppComponent,
    CapabilityStatementComponent,
    SettingsComponent,
    HomeComponent,
    FhirPathComponent,
    MappingLanguageComponent,
    IgsComponent,
    TransformComponent,
    ValidateComponent,
    OperationOutcomeComponent,
    UploadComponent,
  ],
  imports: [
    SharedModule,
    HttpClientModule,
    HighlightModule,
    RouterModule.forRoot(routes, {
      useHash: true,
      relativeLinkResolution: 'legacy',
    }),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient],
      },
    }),
    OAuthModule.forRoot(),
  ],
  providers: [
    {
      provide: HIGHLIGHT_OPTIONS,
      useValue: {
        coreLibraryLoader: () => import('highlight.js/lib/core'),
        lineNumbersLoader: () => import('highlightjs-line-numbers.js'), // Optional, only if you want the line numbers
        languages: {
          json: () => import('highlight.js/lib/languages/json'),
          xml: () => import('highlight.js/lib/languages/xml'),
        },
      },
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
