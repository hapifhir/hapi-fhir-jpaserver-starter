package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;

@Entity
@Table(name = "parent_encounter_map_helper")
public class ParentEncounterMapHelper {
	@Id
	@GeneratedValue
	private long id;

	@Column(name = "encounter_id", nullable = false)
	private String encounterId;

	// Default constructor
	public ParentEncounterMapHelper() {}

	public ParentEncounterMapHelper(String encounterId) {
		this.encounterId = encounterId;
	}

	public long getId() {
		return this.id;
	}

	public String getEncounterId() {
		return this.encounterId;
	}

	public void setEncounterId(String encounterId) {
		this.encounterId = encounterId;
	}

	public String toString() {
		return "PatientEncounterMapper[encounterId="+encounterId+"]";
	}
}
