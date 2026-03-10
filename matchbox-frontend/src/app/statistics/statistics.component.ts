import { AfterViewInit, Component } from '@angular/core';
import FhirClient from 'fhir-kit-client'
import { FhirConfigService } from '../fhirConfig.service';

@Component({
  selector: 'app-statistics',
  imports: [],
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.scss',
})
export class StatisticsComponent implements AfterViewInit {
  operationOutcomes: any[] = [];
  client: FhirClient;

  constructor(private data: FhirConfigService) {
    this.client = data.getFhirClient();
  }

  ngAfterViewInit(): void {
    const url = '/OperationOutcome';
    this.client.request(url)
      .then()
  }

}
