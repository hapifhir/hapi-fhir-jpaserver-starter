import { IssueSeverity, OperationResult } from '../util/operation-result';
import {ValidationParameter, ValidationParameterDefinition} from "./validation-parameter";

export class ValidationEntry {
  readonly filename: string; // "package/package.json",
  readonly resource: string;
  resourceType: string;
  resourceId: string;
  readonly mimetype: string;
  result: OperationResult | undefined;
  aiRecommendation: string;
  readonly profiles: string[] = [];
  selectedProfile: string;
  ig?: string;
  readonly date: Date;
  readonly validationParameters: ValidationParameter[] = [];
  public loading: boolean = false;

  constructor(filename: string, resource: string, mimetype: string | null, profiles: string[] | null, settings: ValidationParameter[] = []) {
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

    if (profiles) {
      this.profiles = profiles;
    }
    this.date = new Date();

    if (this.mimetype === 'application/fhir+json') {
      this.extractJsonInfo();
    } else {
      this.extractXmlInfo();
    }

    if (this.profiles && this.profiles.length) {
      this.selectedProfile = this.profiles[0];
    } else if (this.resourceType) {
      this.selectedProfile = 'http://hl7.org/fhir/StructureDefinition/' + this.resourceType;
    }
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

  extractJsonInfo(): void {
    const res = <fhir.r4.Resource>JSON.parse(this.resource);
    if (res?.resourceType) {
      this.resourceType = res.resourceType;
      this.resourceId = res.id;
    }
    if (res.meta?.profile) {
      this.profiles.push(...res.meta.profile);
    }
  }

  extractXmlInfo(): void {
    let pos = this.resource.indexOf('<?') + 1;
    let posLeft = this.resource.indexOf('<', pos);
    let posRight = this.resource.indexOf('>', posLeft);
    if (posLeft < posRight) {
      let tag = this.resource.substring(posLeft + 1, posRight - 1);
      let posTag = tag.indexOf(' xmlns');
      if (posTag > 0) {
        tag = tag.substring(0, posTag);
      }
      posTag = tag.indexOf(':');
      if (posTag > 0) {
        tag = tag.substring(posTag + 1);
      }
      this.resourceType = tag;

      let posProfileLeft = this.resource.indexOf('profile', posRight);
      if (posProfileLeft > 0) {
        let posProfileValue = this.resource.indexOf('value="', posProfileLeft) + 7;
        let posProfileValueRight = this.resource.indexOf('"', posProfileValue);
        if (posProfileValue < posProfileValueRight) {
          this.profiles.push(this.resource.substring(posProfileValue, posProfileValueRight));
        }
      }
    }
  }
}
