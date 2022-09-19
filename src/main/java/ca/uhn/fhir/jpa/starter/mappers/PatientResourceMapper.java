package ca.uhn.fhir.jpa.starter.mappers;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;

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
  public IBaseResource mapToResource(ResultSet table) throws SQLException {
    String patientID = table.getString("PatientID");
    String patientGender = table.getString("PatientGender");
    String patientDateOfBirth = table.getString("PatientDateOfBirth");
    String patientRace = table.getString("PatientRace");
    String patientMaritalStatus = table.getString("PatientMaritalStatus");
    String patientLanguage = table.getString("PatientLanguage");
    Double patientPopulationPercentageBelowPoverty = table.getDouble("PatientPopulationPercentageBelowPoverty");

    JsonObject jsonObj = Json.createObjectBuilder()
        .add("resourceType", "Patient")
        .add("id", patientID)
        .add("gender", patientGender)
        .add("birthDate", patientDateOfBirth.substring(0, 10))
        .add("maritalStatus", Json.createObjectBuilder().add("text", patientMaritalStatus).build())
        .add("meta", Json.createObjectBuilder().add("lastUpdated", table.getDate("ts").toString()).build())
        .build();
    
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = Json.createWriter(stringWriter);
    writer.writeObject(jsonObj);
    writer.close();
    String fhirText = stringWriter.getBuffer().toString();

    return fhirContext.newJsonParser().parseResource(Patient.class, fhirText);
  }

  @Override
  public String mapToTable(IBaseResource resource, DatabaseOperation op) {
    Patient patient = (Patient) resource;
    String id = patient.getIdElement() == null ? null : patient.getIdElement().getIdPart();
    String gender = patient.getGender().getDisplay();
    String birthDate = patient.getBirthDate().toString();
    String maritalStatus = patient.getMaritalStatus().getText();
    String lastUpdated = patient.getMeta().getLastUpdated().toString();

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
