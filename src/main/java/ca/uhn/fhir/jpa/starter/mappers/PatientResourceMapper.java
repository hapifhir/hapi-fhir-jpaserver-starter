package ca.uhn.fhir.jpa.starter.mappers;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;

@Component
public class PatientResourceMapper implements IResourceMapper {
  private final FhirContext fhirContext;

  public PatientResourceMapper(DaoRegistry daoRegistry) {
    this.fhirContext = daoRegistry.getSystemDao().getContext();
  }


  @Override
  public String getResourceName() {
    return "Patient";
  }

  @Override
  public IBaseResource mapToResource(ResultSet table) throws SQLException, IOException {
    String patientID = table.getString("PatientID");
    String patientGender = table.getString("PatientGender");
    String patientDateOfBirth = table.getString("PatientDateOfBirth");
    String patientRace = table.getString("PatientRace");
    String patientMaritalStatus = table.getString("PatientMaritalStatus");
    String patientLanguage = table.getString("PatientLanguage");
    Double patientPopulationPercentageBelowPoverty = table.getDouble("PatientPopulationPercentageBelowPoverty");

    JSONObject jsonObj = new JSONObject();
    jsonObj.put("resourceType", "Patient");
    jsonObj.put("id", patientID);
    jsonObj.put("gender", patientGender.toLowerCase());
    jsonObj.put("birthDate", patientDateOfBirth.substring(0, 10));
    JSONObject maritalObject = new JSONObject();
    maritalObject.put("text", patientMaritalStatus);
    jsonObj.put("maritalStatus", maritalObject);
    JSONObject metaObject = new JSONObject();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    Date lastUpdate = table.getDate("ts");
    metaObject.put("lastUpdated", format.format(lastUpdate));
    jsonObj.put("meta", metaObject);
    
    StringWriter stringWriter = new StringWriter();
    jsonObj.writeJSONString(stringWriter);
    String fhirText = stringWriter.getBuffer().toString();

    return fhirContext.newJsonParser().parseResource(Patient.class, fhirText);
  }

  @Override
  public String mapToTable(IBaseResource resource, DatabaseOperation op) {
    Patient patient = (Patient) resource;
    String id = patient.getIdElement() == null ? null : patient.getIdElement().getIdPart();
    String gender = patient.getGender().getDisplay();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String birthDate = format.format(patient.getBirthDate());
    String maritalStatus = patient.getMaritalStatus().getText();
    String lastUpdated = patient.getMeta().getLastUpdated() == null ? "NULL" : patient.getMeta().getLastUpdated().toString();

    boolean deleted = op.equals(DatabaseOperation.DELETE);

    if (id == null) {
      return String.format(
          "INSERT INTO Patient (PatientGender, PatientDateOfBirth, PatientMaritalStatus, fhir) VALUES ('%s', '%s', '%s', true);",
          gender, birthDate, maritalStatus);
    }

    String updateSql = String.format(
        "UPDATE Patient SET PatientGender = '%s', PatientDateOfBirth = '%s', PatientMaritalStatus = '%s', deleted = %s, fhir = true WHERE PatientID = '%s' AND ts = '%s';", gender, birthDate, maritalStatus, deleted, id, lastUpdated);
    String insertSql = String.format(
        "INSERT INTO Patient (PatientID, PatientGender, PatientDateOfBirth, PatientMaritalStatus, deleted, fhir) VALUES ('%s', '%s', '%s', '%s', %s, true);",
        id, gender, birthDate, maritalStatus, deleted);
    return String.format(
        "IF EXISTS (SELECT 1 FROM Patient WHERE PatientID = '%s') BEGIN %s END ELSE BEGIN %s END", id, updateSql, insertSql);
  }
  
}
