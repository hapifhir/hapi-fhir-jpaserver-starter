import { AfterViewInit, Component } from '@angular/core';
import FhirClient from 'fhir-kit-client'
import { FhirConfigService } from '../fhirConfig.service';
import { FormControl } from '@angular/forms';



@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.scss',
  standalone: false
})
export class StatisticsComponent implements AfterViewInit {
  operationOutcomes: any[] = [];
  client: FhirClient;
  selectedOutcomeId: number | null  = null;
  filterStartDate = new FormControl<Date | null>(null);
  filterEndDate = new FormControl<Date | null>(null);
  filterSelectedSeverity = new FormControl<string[]>([], { nonNullable: true});

  constructor(private data: FhirConfigService) {
    this.client = data.getFhirClient();
  }

  ngAfterViewInit(): void {
    const url = '/OperationOutcome';
    this.client.request(url).then((bundle: fhir.r4.Bundle) => {
      if (bundle.entry?.length) {
        this.operationOutcomes = bundle.entry.map(entry => entry.resource);
        console.log('OperationOutcomes:', this.operationOutcomes);
      }
        
    })
    
  }

  selectRow(id: number) {
    if (this.selectedOutcomeId === id) {
      this.selectedOutcomeId = null;
    } else {
      this.selectedOutcomeId = id;
    }
    console.log('Selected OO ID: ', id);
  }

  filterOutcomes() {
    const startDate = this.filterStartDate.value;
    const endDate = this.filterEndDate.value;
    const selectedSeverities = this.filterSelectedSeverity.value;
    
  }

}
