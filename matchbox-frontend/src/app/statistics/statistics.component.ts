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
  implementationGuides: any[] = [];
  client: FhirClient;
  selectedOutcomeId: number | null  = null;
  filterStartDate = new FormControl<Date | null>(null);
  filterEndDate = new FormControl<Date | null>(null);
  filterSelectedIssues = new FormControl<string[]>([], { nonNullable: true});
  filterIGs = new FormControl<string[]>([], {nonNullable: true});
  hasNextBundle: boolean = false;
  hasPreviousBundle: boolean = false;
  currentBundle: fhir.r4.Bundle;
  currentPage: number = 1;
  totalEntries: number;

  constructor(private data: FhirConfigService) {
    this.client = data.getFhirClient();
  }

  async ngAfterViewInit(): Promise<void> {
    const parameters = {
      resourceType: 'OperationOutcome'        
    }

    await this.loadBundle(parameters);
    this.getImplementationGuides();
  }

  async loadBundle(params: any) {
    try {
      const bundle = await this.client.search(params) as fhir.r4.Bundle;
      this.processBundle(bundle);
      this.currentPage = 1;
      this.getTotalEntries();
    } catch (error) {
      console.error('Error getting OperationOutcomes: ', error);
    }
  }

  processBundle(bundle: fhir.r4.Bundle) {
    this.currentBundle = bundle;
    this.operationOutcomes = bundle.entry?.map(entry => entry.resource as fhir.r4.OperationOutcome) ?? [];
    this.checkNextAndPreviousBundle(bundle);
  }

  getImplementationGuides() {
    this.client.search({
      resourceType: 'ImplementationGuide',
      searchParams: {
        _sort: 'title',
        _count: 1000
      }
    }).then((bundle: fhir.r4.Bundle) => {
      this.implementationGuides = bundle.entry?.map(entry => entry.resource as fhir.r4.ImplementationGuide);
      console.log('ImplementationGuides: ', this.implementationGuides);
    }).catch(error => {
      console.error('Error getting ImpementationGuides: ', error);
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
    const selectedIssues = this.filterSelectedIssues.value;
    const selectedIgs = this.filterIGs.value;
    
    console.log('StartDate value: ', startDate);
    console.log('Enddate value: ', endDate);
    console.log('issues: ', selectedIssues);
    console.log('IGs: ', selectedIgs);

    // initialize parameter object
    const parameter = {
      resourceType: 'OperationOutcome',
      searchParams: {}
    }

    // check if startDate is present
    if (startDate) {
      // convert date into right format
      const formattedStartDate = startDate.toISOString();
      console.log('Formatted Start Date: ', formattedStartDate);

      // add search parameter to parameter object
      parameter.searchParams['_lastUpdated'] = `ge${formattedStartDate}`;
    }

    // do the same thing with the endDate
    if (endDate) {
      const formattedEndDate = endDate.toISOString();

      // check if startDate is already in "_lastUpdated" -> change to list with startDate and endDate
      if (parameter.searchParams['_lastUpdated']) {
        parameter.searchParams['_lastUpdated'] = [parameter.searchParams['_lastUpdated'], `le${formattedEndDate}`];
      } else {
        parameter.searchParams['_lastUpdated'] = `le${formattedEndDate}`;
      }
    }

    if (selectedIgs.length > 0) {
      for (var ig of selectedIgs) {
        parameter.searchParams['ig'] = ig;
      }
    }

    this.loadBundle(parameter);  
  }

  getInfos(outcome: fhir.r4.OperationOutcome) {
    var infos = 0;
    for (let issue of outcome.issue) {
      if (issue.severity === "information") {
        infos++;
      }
    }
    return infos;
  }

  getWarnings(outcome: fhir.r4.OperationOutcome) {
    var warnings = 0;
    for (let issue of outcome.issue) {
      if (issue.severity === "warning") {
        warnings++;
      }
    }
    return warnings;
  }

  getErrors(outcome: fhir.r4.OperationOutcome) {
    var errors = 0;
    for (let issue of outcome.issue) {
      if (issue.severity === "error") {
        errors++;
      }
    }
    return errors;
  }

  getHighestSeverity(outcome: fhir.r4.OperationOutcome) {
    if (this.getErrors(outcome) > 0) {
      return "error";
    } else if (this.getWarnings(outcome) > 0) {
      return "warning";
    } else {
      return "information";
    }
  }

  getFormattedDate(outcome: fhir.r4.OperationOutcome) {
    const lastUpdated = outcome.meta.lastUpdated;
    const date = new Date(lastUpdated);
    const formattedDate = new Intl.DateTimeFormat('de-ch', { year: 'numeric', month: 'numeric', day: 'numeric', hour: 'numeric', minute: 'numeric' }).format(date);

    return formattedDate;
  }

  getSubExtension(outcome: fhir.r4.OperationOutcome, extension: string) {
    const mainExtension = outcome.issue?.[0]?.extension?.find(ext => ext.url === "http://matchbox.health/validation");
    const subExtension = mainExtension?.extension?.find(ext => ext.url === extension);
    return subExtension;
  }

  getProfile(outcome: fhir.r4.OperationOutcome) {
    const subExtension = this.getSubExtension(outcome, 'profile');
    return subExtension?.valueUri;
  }

  getIg(outcome: fhir.r4.OperationOutcome) {
    const subExtension = this.getSubExtension(outcome, 'ig');
    return subExtension?.valueString;
  }

  getRuntime(outcome: fhir.r4.OperationOutcome) {
    const subExtension = this.getSubExtension(outcome, 'total');
    return subExtension?.valueDuration?.value;
  }

  checkNextAndPreviousBundle(bundle: fhir.r4.Bundle) {
    // check if bundle has a next or previous link
    const nextLink = bundle.link?.find(l => l.relation === "next");
    const previousLink = bundle.link?.find(l => l.relation === "previous");

    if (nextLink) {
      this.hasNextBundle = true;
    } else {
      this.hasNextBundle = false;
    }

    if (previousLink) {
      this.hasPreviousBundle = true;
    } else {
      this.hasPreviousBundle = false;
    }
  }

  async navigateBundle(relation: 'next' | 'previous') {
    const link = this.currentBundle.link?.find(l => l.relation === relation);
    if (link?.url) {
      const urlParameter = new URL(link.url).search;
      const bundle = await this.client.request(urlParameter) as fhir.r4.Bundle;
      this.processBundle(bundle);
    }
  }

  switchToNextSite() {
    this.navigateBundle('next');
    this.currentPage++;
  }

  switchToPreviousSite() {
    this.navigateBundle('previous');
    this.currentPage--;
  }

  calculateAmountOfEntriesShown() {
    const multiplier = this.currentPage - 1;
    const listLength = this.operationOutcomes.length;

    if (listLength > 0) {
      const startRange = multiplier * 20 + 1;
      const endRange = multiplier * 20 + listLength;
      return `${startRange} - ${endRange}`;
    }
  }

  async getTotalEntries() {
    if (this.currentBundle.total) {
      this.totalEntries = this.currentBundle.total;
    } else {
      const link = this.currentBundle.link?.find(l => l.relation === 'next');
      if (link?.url) {
        const urlParameter = new URL(link.url).search;
        const bundle = await this.client.request(urlParameter) as fhir.r4.Bundle;
        this.totalEntries = bundle.total;
      }
    }
  }
}
