package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import org.hl7.fhir.r4.model.*;
import org.smartregister.extension.rest.LocationHierarchyResourceProvider;
import org.smartregister.extension.rest.PractitionerDetailsResourceProvider;
import org.smartregister.model.location.*;
import org.smartregister.model.practitioner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;
import static org.smartregister.utils.Constants.LOCATION;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    // Add your own customization here
	  registerLocationHierarchyTypes();
	  registerPracitionerDetailsTypes();
  }

  private void registerLocationHierarchyTypes() {
	  IFhirResourceDao<Location> locationIFhirResourceDao = daoRegistry.getResourceDao(LOCATION);
	  LocationHierarchyResourceProvider locationHierarchyResourceProvider = new LocationHierarchyResourceProvider();
	  locationHierarchyResourceProvider.setLocationIFhirResourceDao(locationIFhirResourceDao);

	  registerProvider(locationHierarchyResourceProvider);
	  getFhirContext().registerCustomType(LocationHierarchy.class);
	  getFhirContext().registerCustomType(LocationHierarchyTree.class);
	  getFhirContext().registerCustomType(Tree.class);
	  getFhirContext().registerCustomType(ParentChildrenMap.class);
	  getFhirContext().registerCustomType(SingleTreeNode.class);
	  getFhirContext().registerCustomType(TreeNode.class);
	  getFhirContext().registerCustomType(ChildTreeNode.class);
  }


	private void registerPracitionerDetailsTypes() {
		IFhirResourceDao<Practitioner> practitionerIFhirResourceDao = daoRegistry.getResourceDao("Practitioner");
		IFhirResourceDao<PractitionerRole> practitionerRoleIFhirResourceDao = daoRegistry.getResourceDao("PractitionerRole");
		IFhirResourceDao<CareTeam> careTeamIFhirResourceDao = daoRegistry.getResourceDao("CareTeam");
		IFhirResourceDao<OrganizationAffiliation> organizationAffiliationIFhirResourceDao = daoRegistry.getResourceDao("OrganizationAffiliation");
		IFhirResourceDao<Organization> organizationIFhirResourceDao = daoRegistry.getResourceDao("Organization");
		IFhirResourceDao<Location> locationIFhirResourceDao = daoRegistry.getResourceDao(LOCATION);
		LocationHierarchyResourceProvider locationHierarchyResourceProvider = new LocationHierarchyResourceProvider();
		locationHierarchyResourceProvider.setLocationIFhirResourceDao(locationIFhirResourceDao);
		PractitionerDetailsResourceProvider practitionerDetailsResourceProvider = new PractitionerDetailsResourceProvider();
		practitionerDetailsResourceProvider.setPractitionerIFhirResourceDao(practitionerIFhirResourceDao);
		practitionerDetailsResourceProvider.setPractitionerRoleIFhirResourceDao(practitionerRoleIFhirResourceDao);
		practitionerDetailsResourceProvider.setCareTeamIFhirResourceDao(careTeamIFhirResourceDao);
		practitionerDetailsResourceProvider.setOrganizationAffiliationIFhirResourceDao(organizationAffiliationIFhirResourceDao);
		practitionerDetailsResourceProvider.setLocationHierarchyResourceProvider(locationHierarchyResourceProvider);
		practitionerDetailsResourceProvider.setOrganizationIFhirResourceDao(organizationIFhirResourceDao);
		practitionerDetailsResourceProvider.setLocationIFhirResourceDao(locationIFhirResourceDao);

		registerProvider(practitionerDetailsResourceProvider);
		getFhirContext().registerCustomType(PractitionerDetails.class);
		getFhirContext().registerCustomType(KeycloakUserDetails.class);
		getFhirContext().registerCustomType(UserBioData.class);
		getFhirContext().registerCustomType(FhirPractitionerDetails.class);
		getFhirContext().registerCustomType(FhirCareTeamExtension.class);
		getFhirContext().registerCustomType(FhirOrganizationExtension.class);
	}

}
