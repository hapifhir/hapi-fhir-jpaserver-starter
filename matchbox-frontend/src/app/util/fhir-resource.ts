import ResourceBase = fhir.r4.ResourceBase;

export class FhirResource {
  private readonly json: ResourceBase | undefined;
  private readonly xml: Document | undefined;

  constructor(public readonly filename: string,
              public readonly content: string) {
    const extension = filename.split('.').pop()?.toLowerCase();
    if (extension === 'json') {
      try {
        this.json = JSON.parse(content) as ResourceBase;
      } catch (e) {
        console.error('Error parsing JSON content: ', e);
        return undefined;
      }
    } else if (extension === 'xml') {
      try {
        const parser = new DOMParser();
        this.xml = parser.parseFromString(content, 'application/xml') as Document;
      } catch (e) {
        console.error('Error parsing XML content: ', e);
        return undefined;
      }
    }
  }

  resourceType(): string | undefined {
    if (this.xml) {
      const root = this.xml.documentElement;
      if (root) {
        return root.nodeName;
      }
    } else if (this.json) {
      return this.json.resourceType;
    }
    return undefined;
  }

  id(): string | undefined {
    if (this.xml) {
      const idElement = this.xml.querySelector('id');
      if (idElement) {
        return idElement.textContent || undefined;
      }
    } else if (this.json) {
      return (this.json.id as string) || undefined;
    }
    return undefined;
  }

  profiles() : string[] | undefined {
    if (this.xml) {
      const profileElements = this.xml.querySelectorAll('meta > profile');
      if (profileElements) {
        return Array.from(profileElements).map((el) => el.textContent || '').filter((text) => text !== '');
      } else if (this.json && this.json.meta?.profile) {
        return this.json.meta.profile as string[];
      }
    }
    return undefined;
  }

  url(): string | undefined {
    if (this.xml) {
      const urlElement = this.xml.querySelector('url');
      if (urlElement) {
        return urlElement.textContent || undefined;
      }
    } else if (this.json && 'url' in this.json) {
      return this.json.url as string;
    }
    return undefined;
  }
}
