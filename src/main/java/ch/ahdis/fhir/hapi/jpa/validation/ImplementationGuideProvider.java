package ch.ahdis.fhir.hapi.jpa.validation;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionEntity;
import ca.uhn.fhir.jpa.packages.PackageDeleteOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;

/**
 * $load and $load-all Operation for ImplementationGuide Resource
 *
 */
@DisallowConcurrentExecution
public class ImplementationGuideProvider extends ca.uhn.fhir.jpa.rp.r4.ImplementationGuideResourceProvider implements  Job, ApplicationContextAware {

  @Override
  public MethodOutcome delete(HttpServletRequest theRequest, IIdType theResource, String theConditional,
      RequestDetails theRequestDetails) {
    ImplementationGuide guide = this.getDao().read(theResource);
    OperationOutcome oo = uninstall(guide);
    MethodOutcome outcome =   super.delete(theRequest, theResource, theConditional, theRequestDetails);
    if (oo!=null) {
      outcome.setOperationOutcome(oo);
    }
    return outcome;
  }

  @Override
  public MethodOutcome update(HttpServletRequest theRequest, ImplementationGuide theResource, IIdType theId,
      String theConditional, RequestDetails theRequestDetails) {
    OperationOutcome oo = load(theResource);
    MethodOutcome outcome =  super.update(theRequest, theResource, theId, theConditional, theRequestDetails);
    outcome.setOperationOutcome(oo);
    return outcome;
  }

  @Override
  public MethodOutcome create(HttpServletRequest theRequest, ImplementationGuide theResource, String theConditional,
    RequestDetails theRequestDetails) {
    OperationOutcome oo = load(theResource);
    MethodOutcome outcome =  new MethodOutcome();
    outcome.setOperationOutcome(oo);
    return outcome;
  }

  @Autowired
  MatchboxPackageInstallerImpl packageInstallerSvc;

  @Autowired
  AppProperties appProperties;

  @Autowired
  private INpmPackageVersionDao myPackageVersionDao;

  @Autowired
  private PlatformTransactionManager myTxManager;
  
  public OperationOutcome getOperationOutcome(PackageInstallOutcomeJson pkgOutcome) {
    if (pkgOutcome==null) {
      return null;
    }
    OperationOutcome outcome = new OperationOutcome();
    for(String message: pkgOutcome.getMessage())  {
      outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING).setDiagnostics(message);
    }
    for(String resource: pkgOutcome.getResourcesInstalled().keySet())  {
      outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING).setDiagnostics(resource + ": "+pkgOutcome.getResourcesInstalled().get(resource));
    }
    return outcome;
  }

  public OperationOutcome getOperationOutcome(PackageDeleteOutcomeJson pkgOutcome) {
    if (pkgOutcome==null) {
      return null;
    }
    OperationOutcome outcome = new OperationOutcome();
    for(String message: pkgOutcome.getMessage())  {
      outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING).setDiagnostics(message);
    }
    return outcome;
  }

  public OperationOutcome uninstall(ImplementationGuide theResource) {
    return getOperationOutcome(packageInstallerSvc.uninstall(new PackageInstallationSpec()
        .setPackageUrl(theResource.getUrl())
        .setName(theResource.getName())
        .setVersion(theResource.getVersion())));
  }

  public PackageInstallOutcomeJson load(ImplementationGuide theResource, PackageInstallOutcomeJson install) {
    PackageInstallOutcomeJson installOutcome = packageInstallerSvc.install(new PackageInstallationSpec()
        .setPackageUrl(theResource.getUrl())
        .addInstallResourceTypes("NamingSystem",
            "CodeSystem",
            "ValueSet",
            "StructureDefinition",
            "ConceptMap",
            "SearchParameter",
            "Subscription",
            "StructureMap",
            "Questionnaire",
            "ImplementationGuide")
        .setName(theResource.getName())
        .setVersion(theResource.getVersion())
          .setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL));
    
    if (install != null) {
      install.getMessage().addAll(installOutcome.getMessage());
      return install;
    }
    return installOutcome;
  }

	public OperationOutcome load(ImplementationGuide theResource) {
	  PackageInstallOutcomeJson installOutcome = packageInstallerSvc.install(new PackageInstallationSpec()
        .setPackageUrl(theResource.getUrl())
        .addInstallResourceTypes("NamingSystem",
            "CodeSystem",
            "ValueSet",
            "StructureDefinition",
            "ConceptMap",
            "SearchParameter",
            "Subscription",
            "StructureMap",
            "Questionnaire",
            "ImplementationGuide")
        .setName(theResource.getName())
        .setVersion(theResource.getVersion())
          .setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL));
    return getOperationOutcome(installOutcome);
	}
	
	public PackageInstallOutcomeJson loadAll(boolean replace) {
	  PackageInstallOutcomeJson installOutcome = null;
    if (appProperties.getImplementationGuides() != null) {
      Map<String, AppProperties.ImplementationGuide> guides = appProperties.getImplementationGuides();
      for (AppProperties.ImplementationGuide guide : guides.values()) {
        boolean exists = new TransactionTemplate(myTxManager).execute(tx -> {
          Optional<NpmPackageVersionEntity> existing = myPackageVersionDao.findByPackageIdAndVersion(guide.getName(), guide.getVersion());
          return existing.isPresent();
        });
        if (!exists || replace) {
          ImplementationGuide ig = new ImplementationGuide();
          ig.setName(guide.getName());
          ig.setPackageId(guide.getName());
          ig.setUrl(guide.getUrl());
          ig.setVersion(guide.getVersion());
          installOutcome = load(ig, installOutcome);
        }
      }
    }
    return installOutcome;
	}
	
	@Operation(name = "$load-all", type = ImplementationGuide.class, idempotent = false)
	public OperationOutcome loadAll() {
	  return this.getOperationOutcome(loadAll(true));
	}

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      context.getScheduler().unscheduleJob(context.getTrigger().getKey());
      this.loadAll(false);
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }
}
