import { HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { provideTranslateService, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { AppComponent } from './app.component';
import { CapabilityStatementComponent } from './capability-statement/capability-statement.component';
import { HomeComponent } from './home/home.component';
import { MappingLanguageComponent } from './mapping-language/mapping-language.component';
import { SettingsComponent } from './settings/settings.component';
import { SharedModule } from './shared/shared.module';
import { IgsComponent } from './igs/igs.component';
import { HIGHLIGHT_OPTIONS, HighlightModule } from 'ngx-highlightjs';
import { TransformComponent } from './transform/transform.component';
import { ValidateComponent } from './validate/validate.component';
import { OperationResultComponent } from './operation-result/operation-result.component';
import { UploadComponent } from './upload/upload.component';
import { OAuthModule } from 'angular-oauth2-oidc';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { HighlightLineNumbers } from 'ngx-highlightjs/line-numbers';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';
import { HashUrlRedirectionService } from './util/hash-url-redirection-service';
import { APP_BASE_HREF } from '@angular/common';
import { MarkdownModule } from 'ngx-markdown';
import {SortSettingsPipe} from "./validate/order-settings.pipe";

// The Angular routes
// All paths defined here must be supported in matchbox-server's MatchboxStaticResourceConfig, otherwise a direct access
// to the URL will result in a 404 error.
const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
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
    MappingLanguageComponent,
    IgsComponent,
    TransformComponent,
    ValidateComponent,
    OperationResultComponent,
    UploadComponent,
  ],
  bootstrap: [AppComponent],
  imports: [
    SharedModule,
    HighlightModule,
    RouterModule.forRoot(routes, {
      useHash: false, // Move from HashLocationStrategy to PathLocationStrategy
    }),
    OAuthModule.forRoot(),
    NgxMatSelectSearchModule,
    HighlightLineNumbers,
    BrowserAnimationsModule, // Required for toastr
    ToastrModule.forRoot(),
    MarkdownModule.forRoot(),
    SortSettingsPipe
  ],
  providers: [
    provideTranslateService({
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient],
      },
    }),
    {
      provide: HIGHLIGHT_OPTIONS,
      useValue: {
        coreLibraryLoader: () => import('highlight.js/lib/core'),
        lineNumbersLoader: () => import('ngx-highlightjs/line-numbers'), // Optional, only if you want the line numbers
        languages: {
          json: () => import('highlight.js/lib/languages/json'),
          xml: () => import('highlight.js/lib/languages/xml'),
        },
      },
    },
    provideHttpClient(withInterceptorsFromDi()),
    HashUrlRedirectionService,
    { provide: APP_BASE_HREF, useValue: (window as any).MATCHBOX_BASE_PATH },
  ],
})
export class AppModule {}
