package ch.ahdis.matchbox.validation.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.net.URI;

/**
 * The model of a validation item.
 * <p>
 * Copy-pasted from
 * https://gitlab.inria.fr/gazelle/library/validation-service-api/-/blob/master/validation-api/src/main/java/net/ihe/gazelle/validation/api/domain/request/structure/ValidationItem.java?ref_type=heads
 *
 * @author Achraf Achkari
 * @author Quentin Ligier
 **/
@JsonRootName(value = "validationItem")
public class ValidationItem {

	@JsonProperty(value = "itemId")
	private String itemId;

	@JsonProperty(value = "content")
	private byte[] content;

	@JsonProperty(value = "role")
	private String role;

	@JsonProperty(value = "location")
	private String location;

	public String getItemId() {
		return itemId;
	}

	public ValidationItem setItemId(String itemId) {
		this.itemId = itemId;
		return this;
	}

	public byte[] getContent() {
		return content;
	}

	public ValidationItem setContent(byte[] content) {
		this.content = content;
		return this;
	}

	public String getRole() {
		return role;
	}

	public ValidationItem setRole(String role) {
		this.role = role;
		return this;
	}

	public String getLocation() {
		return location;
	}

	public ValidationItem setLocation(String location) {
		this.location = location;
		return this;
	}

	@JsonIgnore
	public boolean isContentValid() {
		return content != null && content.length > 0;
	}

	@JsonIgnore
	public boolean isItemIdValid() {
		return itemId == null || !itemId.isBlank();
	}

	@JsonIgnore
	public boolean isLocationValid(){
		return location == null || isURLValid(location);
	}

	@JsonIgnore
	public boolean isRoleValid(){
		return role == null || !role.isBlank();
	}

	@JsonIgnore
	public boolean isRoleDefined(){
		return role != null && !role.isBlank();
	}

	@JsonIgnore
	public boolean isURLValid(String url) {
		try {
			new URI(url).toURL();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@JsonIgnore
	public boolean isValid(){
		return isContentValid() && isItemIdValid() && isLocationValid() && isRoleValid();
	}

	public static ValidationItem clone(ValidationItem validationItem) {
		return new ValidationItem()
			.setItemId(validationItem.getItemId())
			.setContent(validationItem.getContent() != null ? validationItem.getContent().clone():null)
			.setRole(validationItem.getRole())
			.setLocation(validationItem.getLocation());
	}
}
