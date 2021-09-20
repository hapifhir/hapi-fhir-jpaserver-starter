package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import org.hl7.fhir.r4.model.Location;
import org.smartregister.extension.model.*;
import org.smartregister.extension.rest.LocationHierarchyResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;
import static org.smartregister.extension.utils.Constants.LOCATION;

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

}
