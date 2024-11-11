package ch.ahdis.matchbox.validation.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class Metadata {
	private String name;
	private String value;

	public Metadata() {
	}

	public String getName() {
		return this.name;
	}

	public Metadata setName(String name) {
		this.name = name;
		return this;
	}

	public String getValue() {
		return this.value;
	}

	public Metadata setValue(String value) {
		this.value = value;
		return this;
	}

	@JsonIgnore
	public boolean isNameValid() {
		return this.name != null && !this.name.isBlank();
	}

	@JsonIgnore
	public boolean isValueValid() {
		return this.value == null || !this.value.isBlank();
	}

	@JsonIgnore
	public boolean isValid() {
		return this.isNameValid() && this.isValueValid();
	}

	static Metadata clone(Metadata metadata) {
		return metadata == null ? null : new Metadata().setName(metadata.getName()).setValue(metadata.getValue());
	}
}
