package ca.uhn.fhir.jpa.starter.interceptors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.mappers.DatabaseOperation;
import ca.uhn.fhir.jpa.starter.mappers.IResourceMapper;
import ca.uhn.fhir.jpa.starter.mappers.ResourceMapperRegistry;
import ca.uhn.fhir.rest.api.server.RequestDetails;

@Component
@Interceptor
public class PushDataInterceptor {
  private String connectionString;
  private ResourceMapperRegistry mapperRegistry;

  public PushDataInterceptor(@Value("${mappingtable.jdbc}") String connectionString, ResourceMapperRegistry mapperRegistry) {
    this.connectionString = connectionString;
    this.mapperRegistry = mapperRegistry;
  }

  @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
  public void pushCreateData(IBaseResource resource, RequestDetails details) {
    /* Push to mapping table */
    try (Connection conn = DriverManager.getConnection(connectionString)) {
      IResourceMapper mapper = mapperRegistry.getMapper(details.getResourceName());
      String sql = mapper.mapToTable(resource, DatabaseOperation.INSERT);
      PreparedStatement stat = conn.prepareStatement(sql);
      stat.executeUpdate();
    } catch (SQLException e) {
    }
    throw new AbortDatabaseOperationException();
  }

  @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
  public void pushUpdateData(IBaseResource resource, RequestDetails details) {
    /* Push to mapping table */
    try (Connection conn = DriverManager.getConnection(connectionString)) {
      IResourceMapper mapper = mapperRegistry.getMapper(details.getResourceName());
      String sql = mapper.mapToTable(resource, DatabaseOperation.UPDATE);
      PreparedStatement stat = conn.prepareStatement(sql);
      stat.executeUpdate();
    } catch (SQLException e) {
    }
    throw new AbortDatabaseOperationException();
  }

  @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_DELETED)
  public void pushDeleteData(IBaseResource resource, RequestDetails details) {
    /* Push to mapping table */
    try (Connection conn = DriverManager.getConnection(connectionString)) {
      IResourceMapper mapper = mapperRegistry.getMapper(details.getResourceName());
      String sql = mapper.mapToTable(resource, DatabaseOperation.DELETE);
      PreparedStatement stat = conn.prepareStatement(sql);
      stat.executeUpdate();
    } catch (SQLException e) {
    }
    throw new AbortDatabaseOperationException();
  }
}
