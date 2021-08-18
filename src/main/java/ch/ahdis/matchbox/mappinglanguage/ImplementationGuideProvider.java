package ch.ahdis.matchbox.mappinglanguage;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r5.context.SimpleWorkerContext;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ch.ahdis.matchbox.provider.SimpleWorkerContextProvider;

public class ImplementationGuideProvider extends SimpleWorkerContextProvider<ImplementationGuide> {
  
  public final static String IG_LOAD = "IG_LOAD";
  
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener listener) {
      this.pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
      this.pcs.removePropertyChangeListener(listener);
  }
  
  public ImplementationGuideProvider(SimpleWorkerContext simpleWorkerContext) {
    super(simpleWorkerContext, ImplementationGuide.class);
  }
  

  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImplementationGuideProvider.class);
  
  @Create
  public MethodOutcome create(@ResourceParam ImplementationGuide theResource) {
    if (theResource.getPackageId()!=null) {
      String ig = theResource.getPackageId();
      if (theResource.getVersion()!=null) {
        ig += "#" + theResource.getVersion();
      }
      this.pcs.firePropertyChange(IG_LOAD, null, ig);
      return new MethodOutcome()
          .setCreated(true);
//          .setResource(theResource)
//          .setId(theResource.getIdElement());
    }
    return null;
  }
  
}
