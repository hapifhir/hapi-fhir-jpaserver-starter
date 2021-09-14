package ch.ahdis.fhir.hapi.jpa.validation;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.OperationOutcome;
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
  public MethodOutcome create(HttpServletRequest theRequest, ImplementationGuide theResource, String theConditional,
      RequestDetails theRequestDetails) {
    OperationOutcome outcome = load(theResource);
    if (outcome==null) {
      return super.create(theRequest, theResource, theConditional, theRequestDetails);
    }
    return new MethodOutcome(outcome);
  }

  @Autowired
  MatchboxPackageInstallerImpl packageInstallerSvc;

  @Autowired
  AppProperties appProperties;

  @Autowired
  private INpmPackageVersionDao myPackageVersionDao;

  @Autowired
  private PlatformTransactionManager myTxManager;

	public OperationOutcome load(ImplementationGuide theResource) {
    packageInstallerSvc.install(new PackageInstallationSpec()
        .setPackageUrl(theResource.getUrl())
        .addInstallResourceTypes("NamingSystem",
            "CodeSystem",
            "ValueSet",
            "StructureDefinition",
            "ConceptMap",
            "SearchParameter",
            "Subscription",
            "StructureMap",
            "Questionnaire")
        .setName(theResource.getName())
        .setVersion(theResource.getVersion())
          .setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL));
    return null;
	}
	
	public void loadAll(boolean replace) {
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
          OperationOutcome outcome = load(ig);
          if (outcome==null) {
            packageInstallerSvc.create(ig,new PackageInstallOutcomeJson());
          }
        }
      }
    }
	}
	
	@Operation(name = "$load-all", type = ImplementationGuide.class, idempotent = false)
	public OperationOutcome loadAll() {
	  this.loadAll(true);
		  OperationOutcome outcome = new OperationOutcome();
	      return outcome;
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
