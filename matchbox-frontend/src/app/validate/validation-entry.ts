import {IssueSeverity, OperationResult} from '../util/operation-result';
import {ValidationParameter} from "./validation-parameter";
import { parseFhirResource } from '../util/fhir-resource-parser';

export class ValidationEntry {
  readonly filename: string; // "package/package.json",
  readonly resource: string;
  resourceType: string;
  resourceId: string;
  readonly mimetype: string;
  result: OperationResult | undefined;
  readonly extractedProfiles: string[] = [];
  validationProfile: string;
  ig?: string;
  readonly date: Date;
  readonly validationParameters: ValidationParameter[] = [];
  public loading: boolean = false;

  constructor(filename: string,
              resource: string,
              mimetype: string | null,
              settings: ValidationParameter[] = [],
              validationProfile: string | null = null) {
    this.filename = filename;
    this.resource = resource;
    this.validationParameters = settings;

    if (mimetype) {
      this.mimetype = mimetype;
    } else {
      if (filename.endsWith('.json')) {
        this.mimetype = 'application/fhir+json';
      } else {
        this.mimetype = 'application/fhir+xml';
      }
    }

    this.date = new Date();
    this.validationProfile = validationProfile;

    const parsed = parseFhirResource(filename, resource);
    this.resourceType = parsed.resourceType;
    this.resourceId = parsed.id;
    this.extractedProfiles.push(...parsed.profiles);
  }

  getErrors(): number | undefined {
    if (this.result) {
      return this.result.issues.filter((issue) => issue.severity === IssueSeverity.Error || issue.severity === IssueSeverity.Fatal)
        .length;
    }
    return undefined;
  }

  getWarnings(): number | undefined {
    if (this.result) {
      return this.result.issues.filter((issue) => issue.severity === IssueSeverity.Warning).length;
    }
    return undefined;
  }

  getInfos(): number | undefined {
    if (this.result) {
      return this.result.issues.filter((issue) => issue.severity === IssueSeverity.Information).length;
    }
    return undefined;
  }

  setOperationOutcome(operationOutcome: fhir.r4.OperationOutcome): void {
    this.result = OperationResult.fromOperationOutcome(operationOutcome);
  }
}
