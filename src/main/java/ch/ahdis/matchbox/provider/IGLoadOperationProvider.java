package ch.ahdis.matchbox.provider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;

/**
 * $load and $load-all Operation for ImplementationGuide Resource
 *
 */
public class IGLoadOperationProvider implements PropertyChangeListener {

	@Autowired
	MatchboxPackageInstallerImpl packageInstallerSvc;
	
	@Autowired
	AppProperties appProperties;
	
	 public final static String IG_LOAD = "IG_LOAD";
	  
	  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	  public void addPropertyChangeListener(PropertyChangeListener listener) {
	      this.pcs.addPropertyChangeListener(listener);
	  }

	  public void removePropertyChangeListener(PropertyChangeListener listener) {
	      this.pcs.removePropertyChangeListener(listener);
	  }
	  
	  public IGLoadOperationProvider() {
		  addPropertyChangeListener(this);
	  }
	
	@Operation(name = "$load", type = ImplementationGuide.class, idempotent = false)
	public OperationOutcome load( 
			@OperationParam(name = "url", min = 0, max = 1) String url,
			@OperationParam(name = "name", min = 1, max = 1) String name,
			@OperationParam(name = "version", min = 1, max = 1) String version
			)
	      {
		 this.pcs.firePropertyChange(IG_LOAD, null, name+"#"+version+(url!=null?("#"+url):""));
		       OperationOutcome outcome = new OperationOutcome();
		       return outcome;
		   /*} catch (Exception e) {
			   OperationOutcome outcome = new OperationOutcome();
			   outcome.addIssue().set
		       return outcome;
		   }*/
	     }
	
	@Operation(name = "$load-all", type = ImplementationGuide.class, idempotent = false)
	public OperationOutcome loadAll() {
		  if (appProperties.getImplementationGuides() != null) {
		      Map<String, AppProperties.ImplementationGuide> guides = appProperties.getImplementationGuides();
		      for (AppProperties.ImplementationGuide guide : guides.values()) {
		    	  this.pcs.firePropertyChange(IG_LOAD, null, guide.getName()+"#"+guide.getVersion()+(guide.getUrl()!=null?("#"+guide.getUrl()):"")); 
		      }
		  }	
		  OperationOutcome outcome = new OperationOutcome();
	      return outcome;
	}

	 @Override
	  public synchronized void propertyChange(PropertyChangeEvent evt)  {
	    if (IG_LOAD.equals(evt.getPropertyName())) {
	      String ig[] = ((String) evt.getNewValue()).split("#");
	      try {
	    	  packageInstallerSvc.install(new PackageInstallationSpec()
			          .setPackageUrl(ig.length > 2 ? ig[2] : null)
			          .addInstallResourceTypes("NamingSystem",
			        			"CodeSystem",
			        			"ValueSet",
			        			"StructureDefinition",
			        			"ConceptMap",
			        			"SearchParameter",
			        			"Subscription",
			        			"StructureMap","Questionnaire")
			          .setName(ig[0])
			          .setVersion(ig.length > 1 ? ig[1] : null)
			            .setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL));
	        
	      } catch (Exception e1) {
	        //ourLog.error("Error loading implemenation guide " + ig);
	        e1.printStackTrace();
	      }
	    }
	  }
}
