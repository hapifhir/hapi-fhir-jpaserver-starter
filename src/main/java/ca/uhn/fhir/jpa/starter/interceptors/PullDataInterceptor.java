package ca.uhn.fhir.jpa.starter.interceptors;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.starter.mappers.IResourceMapper;
import ca.uhn.fhir.jpa.starter.mappers.ResourceMapperRegistry;

@Component
@Interceptor
public class PullDataInterceptor {
  private String connectionString;
  private DaoRegistry daoRegistry;
  private ResourceMapperRegistry mapperRegistry;

  public PullDataInterceptor(@Value("${mappingtable.jdbc}") String connectionString, DaoRegistry daoRegistry, ResourceMapperRegistry mapperRegistry) {
    this.connectionString = connectionString;
    this.daoRegistry = daoRegistry;
    this.mapperRegistry = mapperRegistry;
  }

  @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
  public boolean pullDataBeforeRequest(HttpServletRequest req, HttpServletResponse resp) {
    String resourceName = req.getContextPath().substring(1);
    String selectSql = String.format("SELECT * FROM %s WHERE emr = true;", resourceName);
    try (Connection conn = DriverManager.getConnection(connectionString)) {
      /* 1 Pull mapping table */
      PreparedStatement stat = conn.prepareStatement(selectSql);
      ResultSet result = stat.executeQuery();

      /* 2 Update Fhir Database using DAO and Mapper */
      IFhirResourceDao<IBaseResource> dao = daoRegistry.getResourceDao(resourceName);
      IResourceMapper mapper = mapperRegistry.getMapper(resourceName);
      Date latest = null; /* Remember latest timestamp reflected */
      while (result.next()) {
        Date timestamp = result.getDate("ts");
        if (latest == null || latest.before(timestamp)) {
          latest = timestamp;
        }

        boolean deleteFlag = result.getBoolean("deleted");
        IBaseResource resource = mapper.mapToResource(result);
        if (deleteFlag) {
          dao.delete(resource.getIdElement());
        } else {
          dao.update(resource);
        }
      }

      if (req.getMethod().equals(HttpMethod.GET.toString())) {
        /* 3 Delete Mapping table */
        String deleteSql = String.format("DELETE FROM %s WHERE ts < ? AND emr = true", resourceName);
        PreparedStatement deleteStat = conn.prepareStatement(deleteSql);
        deleteStat.setDate(1, latest);
        deleteStat.executeUpdate();
      }
      return true;
    } catch (SQLException e) {
      return false;
    }
  }
}
