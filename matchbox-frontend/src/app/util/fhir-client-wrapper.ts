import { Client, FhirResource } from 'fhir-kit-client';
import CapabilityStatement = fhir.r4.CapabilityStatement;
import Resource = fhir.r4.Resource;
import OperationDefinition = fhir.r4.OperationDefinition;
import Bundle = fhir.r4.Bundle;
import OperationOutcome = fhir.r4.OperationOutcome;
import Parameters = fhir.r4.Parameters;
import { ValidationEntry } from '../validate/validation-entry';

/**
 * A wrapper for the FHIR client that provides a simpler API for our needs, with the right types.
 */
export class FhirClientWrapper {
  private readonly client: Client;

  constructor(readonly baseUrl: string) {
    this.client = new Client({
      baseUrl: baseUrl,
    });
  }

  async capabilityStatement(): Promise<CapabilityStatement> {
    const result = await this.client.capabilityStatement();
    return result as unknown as CapabilityStatement;
  }

  async search(params: any): Promise<Bundle> {
    const result = await this.client.search(params);
    return result as unknown as Bundle;
  }

  create(params: any): Promise<OperationOutcome> {
    return this.client.create(params) as unknown as Promise<OperationOutcome>;
  }

  update(params: any): Promise<OperationOutcome> {
    return this.client.update(params) as unknown as Promise<OperationOutcome>;
  }

  delete(params: any): Promise<OperationOutcome> {
    return this.client.delete(params) as unknown as Promise<OperationOutcome>;
  }

  transformFromUrl(structureMapUrl: string, resource: Resource): Promise<Resource> {
    return this.client.operation({
      name: 'transform?source=' + encodeURIComponent(structureMapUrl),
      resourceType: 'StructureMap',
      input: resource as FhirResource,
    }) as Promise<Resource>;
  }

  transformFromParameters(parameters: Parameters): Promise<Resource> {
    return this.client.operation({
      name: 'transform',
      resourceType: 'StructureMap',
      input: parameters as FhirResource,
      options: {
        headers: {
          'content-type': 'application/fhir+json',
        },
      },
    }) as Promise<Resource>;
  }

  async readOperationDefinition(id: string): Promise<OperationDefinition> {
    const result = this.client.read({
      resourceType: 'OperationDefinition',
      id: id,
    });
    return result as unknown as OperationDefinition;
  }

  async nextPage(bundle: Bundle): Promise<Bundle> {
    const result = this.client.nextPage({
      bundle: bundle as unknown as BundleWithLink,
    });
    return result as unknown as Bundle;
  }

  async prevPage(bundle: Bundle): Promise<Bundle> {
    const result = this.client.prevPage({
      bundle: bundle as unknown as BundleWithLink,
    });
    return result as unknown as Bundle;
  }

  async listStructureMaps(): Promise<Bundle> {
    const result = await this.client.operation({
      name: 'list',
      resourceType: 'StructureMap',
      method: 'GET',
    });
    return result as unknown as Bundle;
  }

  async validate(entry: ValidationEntry): Promise<OperationOutcome> {
    const searchParams = new URLSearchParams();
    searchParams.set('profile', entry.validationProfile!!);
    if (entry.ig) {
      searchParams.set('ig', entry.ig);
    }
    // Validation options
    for (const param of entry.validationParameters) {
      searchParams.append(param.name, param.value);
    }
    const result = this.client.operation({
      name: 'validate?' + searchParams.toString(),
      resourceType: undefined,
      // Here we have to cheat because the client expects either a FhirResource or SearchParameters, whereas we want
      // to send a plain string (the resource JSON or XML serialization).
      input: entry.resource as unknown as FhirResource,
      options: {
        headers: {
          accept: 'application/fhir+json',
          'content-type': entry.mimetype,
        },
      },
    });
    return result as unknown as OperationOutcome;
  }
}

type BundleWithLink = FhirResource & {
  link: Array<{
    relation: string;
    url: string;
  }>;
};
