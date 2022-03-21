package ch.ahdis.fhir.hapi.jpa.validation;

import java.util.Comparator;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseHasExtensions;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.util.FhirTerser;

public class Canonicalizer {

  protected FhirContext fhirContext;
  protected FhirTerser fhirTerser;

  public Canonicalizer(FhirContext fhirContext) {
    this.fhirContext = fhirContext;
    this.fhirTerser = this.fhirContext.newTerser();
  }

  private static Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareTo);

  private static Comparator<IBaseExtension<?, ?>> extensionComparator = Comparator
      .comparing(IBaseExtension<?, ?>::getUrl, nullSafeStringComparator);
  
  

  public IBaseResource canonicalize(IBaseResource input) {
    // Resource
    clearField(this.fhirContext, input, "id");
    clearField(this.fhirContext, input, "meta");
    clearField(this.fhirContext, input, "implicitRules");

    // DomainResource TODO

    // ... text 0..1 Narrative Text summary of the resource, for human
    // interpretation
    // ... contained 0..* Resource Contained, inline Resources
    // ... extension 0..* Extension Additional content defined by implementations
    // modifierExtension ?! 0..* Extension Extensions that cannot be ignored
    IBaseHasExtensions baseHasExtensions = validateExtensionSupport(input);
    baseHasExtensions.getExtension().sort(extensionComparator);
    
    // TODO: sort in complex extensions according to the url
    
    // TODO: if we have * cardinality of a specific extension, we would need to
    // order them also, do we need do that really? or should we look that the
    // orginal sort order is retained?

    return input;
  }

  private static BaseRuntimeChildDefinition getBaseRuntimeChildDefinition(FhirContext theFhirContext,
      String theFieldName, IBaseResource theFrom) {
    RuntimeResourceDefinition definition = theFhirContext.getResourceDefinition(theFrom);
    BaseRuntimeChildDefinition childDefinition = definition.getChildByName(theFieldName);
    Validate.notNull(childDefinition);
    return childDefinition;
  }

  public static void clearField(FhirContext theFhirContext, IBaseResource theResource, String theFieldName) {
    BaseRuntimeChildDefinition childDefinition = getBaseRuntimeChildDefinition(theFhirContext, theFieldName,
        theResource);
    childDefinition.getMutator().setValue(theResource, null);
  }

  private static IBaseHasExtensions validateExtensionSupport(IBase theBase) {
    if (!(theBase instanceof IBaseHasExtensions)) {
      throw new IllegalArgumentException(
          Msg.code(1747) + String.format("Expected instance that supports extensions, but got %s", theBase));
    }
    return (IBaseHasExtensions) theBase;
  }

}
