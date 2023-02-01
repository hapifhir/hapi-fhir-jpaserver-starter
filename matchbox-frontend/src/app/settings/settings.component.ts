import { Component, OnInit } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import { Subscription } from 'rxjs';
import debug from 'debug';
import { MatTableDataSource } from '@angular/material/table';
import * as R from 'ramda';
import Client from 'fhir-kit-client';
import { Router } from '@angular/router';
import { ThrowStmt } from '@angular/compiler';

// currently R4 only 'http://localhost:8080/baseDstu3',
// 'http://vonk.furore.com',
// 'http://fhirtest.uhn.ca/baseDstu3'

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements OnInit {
  fhirServers = [
    'https://test.ahdis.ch/matchboxv3/fhir',
    'https://gazelle.ihe.net/matchbox/fhir',
    'https://ehealthsuisse.ihe-europe.net/matchbox/fhir',
    'https://hapi.fhir.org/baseR4',
    'http://hapi.fhir.org/baseR4',
    'http://tx.fhir.org/r4/',
    'http://test.fhir.org/r4',
    'https://test.ahdis.ch/mag-cara/fhir',
    'https://test.ahdis.ch/mag-pmp/fhir',
    'https://test.ahdis.ch/mag-bfh/fhir',
    'https://test.ahdis.ch/mag-test-emedo/fhir',
    'http://localhost:8080/matchboxv3/fhir',
    'http://localhost:9090/mag-pmp/fhir',
    'http://localhost:9090/mag-cara/fhir',
    'https://fhir-mapping-lab.azurewebsites.net/',
  ];

  subscriptionFhir: Subscription;
  baseUrlFhir: string;
  subscriptionMag: Subscription;
  baseUrlMag: string;

  client: Client;

  constructor(private data: FhirConfigService, private router: Router) {
    this.client = data.getFhirClient();
  }

  ngOnInit() {
    this.baseUrlFhir = this.data.getFhirMicroService();
  }

  getFhirSelectedValue(): string {
    return this.baseUrlFhir;
  }

  setFhirSelectedValue(value: string) {
    debug('setting new server to ' + value);
    this.data.changeFhirMicroService(value);
  }
}
