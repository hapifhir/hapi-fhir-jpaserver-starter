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
  client: FhirClient;

  // Server info
  operationOutcomes: any[] = [];
  implementationGuides: any[] = [];
  supportedProfiles: Map <string, any> = new Map();
  isLoading: boolean = false;

  // OperationOutcome table helpers  
  selectedOutcomeId: number | null  = null;

  // Search parameters
  filterStartDate = new FormControl<Date | null>(null);
  filterEndDate = new FormControl<Date | null>(null);
  filterSelectedIssues = new FormControl<string[]>([], { nonNullable: true});
  filterIGs = new FormControl<string[]>([], {nonNullable: true});
  filterProfile = new FormControl<string | null>(null);

  // Pagination helpers
  hasNextBundle: boolean = false;
  hasPreviousBundle: boolean = false;
  currentBundle: fhir.r4.Bundle;
  currentPage: number = 1;
  totalEntries: number = 0;
  
  // Profile filter helpers
  filteredProfilesList: any[] = [];
  profileFilter: string = '';
  

  constructor(private data: FhirConfigService) {
    this.client = data.getFhirClient();
  }

  async ngAfterViewInit(): Promise<void> {
    // search parameters for initial operation outcome view (_total is important for pagination info)
    const parameters = {
      resourceType: 'OperationOutcome',
      searchParams: {
        _total: 'accurate'
      }
    }

    // load and process search result bundle
    await this.loadBundle(parameters);

    // load installed IGs
    this.getImplementationGuides();

    // load supported profile list
    await this.getSupportedProfiles();
  }

  /**
   * Loads a FHIR bundle containing OperationOutcomes based of the search parameters
   * @param params search parameters for FHIR 
   */
  async loadBundle(params: any) {
    // enables loading screen
    this.isLoading = true;
    try {
      const bundle = await this.client.search(params) as fhir.r4.Bundle;

      // sends search result bundle to get processed
      this.processBundle(bundle);

      // sets current page to 1 for UI
      this.currentPage = 1;

      // extracts total amount of entries for pagination info UI
      this.totalEntries = await this.getTotalEntries(bundle);
    } catch (error) {
      console.error('Error getting OperationOutcomes: ', error);
    } finally {
      // disables loading screen
      this.isLoading = false;
    }
  }

  /**
   * Extracts all Operation Outcomes in a bundle to the list, that gets displayed in the UI
   * @param bundle the FHIR bundle
   */
  processBundle(bundle: fhir.r4.Bundle) {
    // sets current displayed bundle for future reference
    this.currentBundle = bundle;

    // maps entries to list
    this.operationOutcomes = bundle.entry?.map(entry => entry.resource as fhir.r4.OperationOutcome) ?? [];

    // sets next and previous bundle booleans
    this.checkNextAndPreviousBundle(bundle);
  }

  /**
   * Loads all installed IGs on the server and maps them to an internal list
   */
  getImplementationGuides() {
    // search for IGs
    this.client.search({
      resourceType: 'ImplementationGuide',
      searchParams: {
        _sort: 'title',
        _count: 1000
      }
    }).then((bundle: fhir.r4.Bundle) => {
      // map entries to list
      this.implementationGuides = bundle.entry?.map(entry => entry.resource as fhir.r4.ImplementationGuide);
    }).catch(error => {
      console.error('Error getting ImpementationGuides: ', error);
    })
  }

  /**
   * Gets the Operation Definition of the server and stores all supported profiles in an internal Map. 
   */
  async getSupportedProfiles() {
    // Gets OperationDefinition
    const od = await this.client.read({
      resourceType: 'OperationDefinition',
      id: '-s-validate'
    }) as fhir.r4.OperationDefinition;

    // Searches parameters for one with name = profile
    od.parameter?.forEach((parameter: fhir.r4.OperationDefinitionParameter) => {
      if (parameter.name === 'profile') {
        // Parses every item in _targetProfile
        parameter._targetProfile?.forEach((item) => {
          // Gets canonical and title from each supported profile, uses helper function
          const canonical = this.getExtensionStringValue(item, 'sd-canonical');
          const title = this.getExtensionStringValue(item, 'sd-title');

          // Puts every supported profile in internal Map
          this.supportedProfiles.set(canonical, title);
        });
      }
    });
    // Initializes list for profile filter
    this.updateProfileFilter();
  }

  /**
   * Helper function to return the string value of an extension
   * @param element the FHIR element
   * @param url the extension url
   * @returns 
   */
  getExtensionStringValue(element: fhir.r4.Element, url: string): string {
    return element.extension?.find(e => e.url === url)?.valueString ?? '';
  }

  /**
   * Updates the list of profiles shown in the UI. Takes input from filter field
   */
  updateProfileFilter() {
    // Converts input value to lower case
    const searchTerm = this.profileFilter.toLowerCase();

    // Creates an array from all entries in supportedProfiles Map
    this.filteredProfilesList = Array.from(this.supportedProfiles.entries())
    // Filters array to check if search input is in profile title or canonical (url)
    .filter(([canonical, title]) => canonical.toLowerCase().includes(searchTerm) || title.toLowerCase().includes(searchTerm));
  }

  /**
   * Helper function of the UI. If selectedOutcomeId is already the same ID (click on the same row again), it gets reset to null.
   * @param id OperationOutcome ID
   */
  selectRow(id: number) {
    if (this.selectedOutcomeId === id) {
      this.selectedOutcomeId = null;
    } else {
      this.selectedOutcomeId = id;
    }
  }

  /**
   * Gets activated by the UI search button. Checks all available search parameters in the UI and creates a custom search parameter.
   * Sends search request to server via loadBundle method
   */
  filterOutcomes() {
    // collects all the search parameters
    const startDate = this.filterStartDate.value;
    const endDate = this.filterEndDate.value;
    const selectedIssues = this.filterSelectedIssues.value;
    const selectedIgs = this.filterIGs.value;
    const selectedProfile = this.filterProfile.value;
    
    // initialize search parameter object
    const parameter = {
      resourceType: 'OperationOutcome',
      searchParams: {
        _total: 'accurate'
      }
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

    // check if any IGs are selected 
    if (selectedIgs.length > 0) {
      // create search parameter entry
      parameter.searchParams['ig'] = '';
      // parse all selected IGs and append them at the end of the search parameter string with a ','
      for (var ig of selectedIgs) {
        parameter.searchParams['ig'] += ig + ',';
      }
    }

    // check if any issues are selected, same concept as IGs
    if (selectedIssues.length > 0) {
      parameter.searchParams['issue'] = '';
      for (var issue of selectedIssues) {
        parameter.searchParams['issue'] += issue + ',';
      }
    }

    // check if a profile is selected
    if (selectedProfile) {
      // create "custom" search parameter for profile with exact tag
      parameter.searchParams['profile:exact'] = selectedProfile;
    }

    // loads search results with search parameters
    this.loadBundle(parameter);  
  }

  /**
   * Gets amount of informational issues (for UI)
   * @param outcome FHIR OperationOutcome
   * @returns number
   */
  getInfos(outcome: fhir.r4.OperationOutcome) {
    var infos = 0;
    for (let issue of outcome.issue) {
      if (issue.severity === "information") {
        infos++;
      }
    }
    return infos;
  }

  /**
   * Gets amount of warning issues (for UI)
   * @param outcome FHIR OperationOutcome
   * @returns number
   */
  getWarnings(outcome: fhir.r4.OperationOutcome) {
    var warnings = 0;
    for (let issue of outcome.issue) {
      if (issue.severity === "warning") {
        warnings++;
      }
    }
    return warnings;
  }

  /**
   * Gets the amount of error issues (for UI)
   * @param outcome FHIR OperationOutcome
   * @returns number
   */
  getErrors(outcome: fhir.r4.OperationOutcome) {
    var errors = 0;
    for (let issue of outcome.issue) {
      if (issue.severity === "error") {
        errors++;
      }
    }
    return errors;
  }

  /**
   * Gets nicely formatted date from lastUpdated field in OperationOutcome
   * @param outcome FHIR OperationOutcome
   * @returns date
   */
  getFormattedDate(outcome: fhir.r4.OperationOutcome) {
    const lastUpdated = outcome.meta.lastUpdated;
    const date = new Date(lastUpdated);
    const formattedDate = new Intl.DateTimeFormat('de-ch', { year: 'numeric', month: 'numeric', day: 'numeric', hour: 'numeric', minute: 'numeric' }).format(date);

    return formattedDate;
  }

  /**
   * Helper method to get sub extension from extension name
   * @param outcome FHIR OperationOutcome
   * @param extension extension name as string
   * @returns subextension
   */
  getSubExtension(outcome: fhir.r4.OperationOutcome, extension: string) {
    // gets main extension from url "http://matchbox.health/validation"
    const mainExtension = outcome.issue?.[0]?.extension?.find(ext => ext.url === "http://matchbox.health/validation");
    // searches main extension for extensions with specified url string
    const subExtension = mainExtension?.extension?.find(ext => ext.url === extension);
    return subExtension;
  }

  /**
   * Gets Profile url from OperationOutcome
   * @param outcome FHIR OperationOutcome
   * @returns string
   */
  getProfile(outcome: fhir.r4.OperationOutcome) {
    const subExtension = this.getSubExtension(outcome, 'profile');
    return subExtension?.valueUri;
  }

  /**
   * Gets IG from OperationOutcome
   * @param outcome FHIR OperationOutcome
   * @returns string
   */
  getIg(outcome: fhir.r4.OperationOutcome) {
    const subExtension = this.getSubExtension(outcome, 'ig');
    return subExtension?.valueString;
  }

  /**
   * Gets total runtime duration from OperationOutcome
   * @param outcome FHIR OperationOutcome
   * @returns number
   */
  getRuntime(outcome: fhir.r4.OperationOutcome) {
    const subExtension = this.getSubExtension(outcome, 'total');
    return subExtension?.valueDuration?.value;
  }

  /**
   * Checks if the bundle is part of a bigger set. Checks if bundle has next or previous relation. Sets internal booleans accordingly
   * @param bundle FHIR Bundle
   */
  checkNextAndPreviousBundle(bundle: fhir.r4.Bundle) {
    // check if bundle has a next or previous link relation
    const nextLink = bundle.link?.find(l => l.relation === "next");
    const previousLink = bundle.link?.find(l => l.relation === "previous");

    // sets hasNextBundle boolean according to findings
    if (nextLink) {
      this.hasNextBundle = true;
    } else {
      this.hasNextBundle = false;
    }

    // sets hasPreviousBundle boolean according to findings
    if (previousLink) {
      this.hasPreviousBundle = true;
    } else {
      this.hasPreviousBundle = false;
    }
  }

  /**
   * Loads next/previous bundle from relation links, same method for both
   * @param relation string "next" or "previous"
   */
  async navigateBundle(relation: 'next' | 'previous') {
    // gets relation from currentBundle 
    const link = this.currentBundle.link?.find(l => l.relation === relation);
    // check if url is present
    if (link?.url) {
      // enable loading screen
      this.isLoading = true;
      try {
        // extract only the parameters from url (cut everything before "?")
        const urlParameter = new URL(link.url).search;
        // request new bundle from server
        const bundle = await this.client.request(urlParameter) as fhir.r4.Bundle;
        // process new bundle
        this.processBundle(bundle);
      } catch (error) {
        console.error('Error navigating bundle: ', error);
      } finally {
        // disable loading screen
        this.isLoading = false;
      }
    }
  }

  /**
   * Button action to switch to next bundle. Uses navigateBundle method. Updates page number
   */
  switchToNextSite() {
    this.navigateBundle('next');
    this.currentPage++;
  }

  /**
   * Button action to switch to previous bundle. Uses navigateBundle method. Updates page number
   */
  switchToPreviousSite() {
    this.navigateBundle('previous');
    this.currentPage--;
  }

  /**
   * Calculates the amount of entries shown on the current page. Used for UI pagination info
   * @returns formatted string
   */
  calculateAmountOfEntriesShown() {
    // sets page multiplier
    const multiplier = this.currentPage - 1;
    // gets the total amount of entries in currently displayed bundle
    const listLength = this.operationOutcomes.length;

    // checks if any entries are displayed
    if (listLength > 0) {
      // sets startrange (example: Page 2 => multiplier = 1 => 1*20+1 = 21)
      const startRange = multiplier * 20 + 1;
      // sets endrange according to amount of entries in list
      const endRange = multiplier * 20 + listLength;
      // returns formatted string for use in UI
      return `${startRange} - ${endRange}`;
    }
  }

  /**
   * Gets the total amount of search entries in bundle set. If not able to get from current Bundle, next Bundle gets requested. 
   * First bundle only has total info, if explicitly set in search parameters. Next / previous bundle always have total info
   * @param bundle FHIR Bundle
   * @returns number
   */
  async getTotalEntries(bundle: fhir.r4.Bundle) {
    // checks if current bundle has total info inside
    if (bundle.total) {
      // return total from current bundle
      return bundle.total;
    } else {
      // check for link to next bundle
      const link = bundle.link?.find(l => l.relation === 'next');
      if (link?.url) {
        // strip url
        const urlParameter = new URL(link.url).search;
        // request next bundle
        const nextBundle = await this.client.request(urlParameter) as fhir.r4.Bundle;
        // return total from next bundle
        return nextBundle.total;
      }
    }
  }

  /**
   * Gets the selected profile title for display in profile filter field
   * @returns 
   */
  getSelectedProfileTitle() {
    // gets the currently selected value
    const selectedCanonical = this.filterProfile.value;

    // if nothing is selected, return ''
    if (!selectedCanonical) {
      return '';
    } else {
      // get and return title from supported Profiles Map
      return this.supportedProfiles.get(selectedCanonical) || selectedCanonical;
    }
  }

  /**
   * Resets all search parameters to their default values. Used by reset button in UI
   */
  resetSearchParameters() {
    this.filterStartDate.reset(null);
    this.filterEndDate.reset(null);
    this.filterIGs.reset([]);
    this.filterSelectedIssues.reset([]);
    this.filterProfile.reset(null);
  }
}
