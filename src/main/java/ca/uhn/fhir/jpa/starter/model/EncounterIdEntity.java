package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;

@Entity
@Table(name = "encounter_id_entity")
public class EncounterIdEntity {
	@Id
	@GeneratedValue
	private long id;

	@Column(name = "encounter_id", nullable = false, unique = true)
	private String encounterId;

	// Default constructor
	public EncounterIdEntity() {}

	public EncounterIdEntity(String encounterId) {
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
		return "EncounterIdEntity[encounterId="+encounterId+"]";
	}
}
