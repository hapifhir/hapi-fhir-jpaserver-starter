import { Component, OnInit } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import Client from 'fhir-kit-client';
import { Router } from '@angular/router';
import packageJson from '../../../package.json';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  client: Client;

  public version: string = packageJson.version;

  constructor(fhirConfigService: FhirConfigService, private router: Router) {
    this.client = fhirConfigService.getFhirClient();
  }

  async ngOnInit() {}
}
