package ch.ahdis.matchbox.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class UnexpectedError {
	private String name;
	private String message;
	private UnexpectedError cause;

	public UnexpectedError() {
	}

	public String getName() {
		return this.name;
	}

	public UnexpectedError setName(String name) {
		this.name = name;
		return this;
	}

	public String getMessage() {
		return this.message;
	}

	public UnexpectedError setMessage(String message) {
		this.message = message;
		return this;
	}

	public UnexpectedError getCause() {
		return this.cause;
	}

	public UnexpectedError setCause(UnexpectedError cause) {
		this.cause = cause;
		return this;
	}

	@JsonIgnore
	public boolean isNameValid() {
		return this.name != null && !this.name.isEmpty();
	}

	@JsonIgnore
	public boolean isMessageValid() {
		return this.message != null && !this.message.isBlank();
	}

	@JsonIgnore
	public boolean isValid() {
		return this.isMessageValid() && this.isNameValid();
	}

	static UnexpectedError clone(UnexpectedError unexpectedError) {
		if (unexpectedError == null) {
			return null;
		} else {
			UnexpectedError clone = new UnexpectedError();
			clone.setName(unexpectedError.getName());
			clone.setMessage(unexpectedError.getMessage());
			if (unexpectedError.getCause() != null) {
				clone.setCause(clone(unexpectedError.getCause()));
			}

			return clone;
		}
	}
}
