import ResourceBase = fhir.r4.ResourceBase;

export interface FhirResource {
  resourceType: string;
  id: string | null;
  profiles: string[];
  url: string | null;
}

export function parseFhirResource(filename: string, content: string): FhirResource | null {
  try {
    const extension = filename.split('.').pop()?.toLowerCase();
    if (extension === 'json') {
      const json = JSON.parse(content) as ResourceBase;
      const resourceType = json.resourceType;
      if (!resourceType) {
        console.info('JSON content does not contain a resourceType property.');
        return null;
      }
      const ret = {
        resourceType,
        id: (json.id as string) || null,
        profiles: (json.meta?.profile as string[]) || [],
        url: 'url' in json ? (json.url as string) : null,
      };
      console.log(ret);
      return ret;
    } else if (extension === 'xml') {
      const parser = new DOMParser();
      const xml = parser.parseFromString(content, 'application/xml') as Document;
      const resourceType = xml.documentElement?.localName;
      if (!resourceType) {
        console.info('XML content does not contain a document element.');
        return null;
      }
      const ret = {
        resourceType,
        id: xml.querySelector('id')?.getAttribute('value') || null,
        profiles:
          Array.from(xml.querySelectorAll('meta > profile'))
            .map((el) => el.getAttribute('value'))
            .filter(text => text != null),
        url: xml.querySelector('url')?.getAttribute('value') || null,
      };
      console.log(ret);
      return ret;
    }
    return null;
  } catch (e) {
    console.error('Error parsing content: ', e);
    return null;
  }
}
