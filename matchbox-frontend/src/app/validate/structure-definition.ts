export class StructureDefinition {
  constructor(
    // The canonical
    public canonical: string,

    // The title
    public title: string,

    // The ImplementationGuide identifier
    public igId: string,

    // The ImplementationGuide version
    public igVersion: string,

    // Whether the ImplementationGuide version is the current one (the higher version)
    public isCurrent: boolean
  ) {}
}
