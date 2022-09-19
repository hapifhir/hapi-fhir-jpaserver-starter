package ca.uhn.fhir.jpa.starter.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hl7.fhir.instance.model.api.IBaseResource;

public interface IResourceMapper {
  public String getResourceName();
  public IBaseResource mapToResource(ResultSet table) throws SQLException;
  public String mapToTable(IBaseResource resource, DatabaseOperation op);
}
