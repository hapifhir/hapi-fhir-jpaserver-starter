package ch.ahdis.matchbox.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
@JsonTypeName("validationProfile")
@JsonPropertyOrder({"profileID", "profileName", "domain", "coveredItems"})
public class ValidationProfile {

	@JsonProperty("profileID")
	private String profileID;

	@JsonProperty("profileName")
	private String profileName;

	// Not really useful for FHIR transactions
	@JsonProperty("coveredItems")
	private List<String> coveredItems = new ArrayList<>();

	// E.g. "IHE", "ITI", "EPR".
	@JsonProperty("domain")
	private String domain;

	public String getDomain() {
		return this.domain;
	}

	public ValidationProfile setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public String getProfileName() {
		return this.profileName;
	}

	public ValidationProfile setProfileName(String profileName) {
		this.profileName = profileName;
		return this;
	}

	public String getProfileID() {
		return this.profileID;
	}

	public ValidationProfile setProfileID(String profileID) {
		this.profileID = profileID;
		return this;
	}

	public List<String> getCoveredItems() {
		return this.coveredItems;
	}

	public ValidationProfile setCoveredItems(List<String> coveredItems) {
		this.coveredItems = coveredItems;
		return this;
	}

	public ValidationProfile addCoveredItem(String item) {
		this.coveredItems.add(item);
		return this;
	}

	@JsonIgnore
	public boolean isProfileNameValid() {
		return this.profileName == null || !this.profileName.isBlank();
	}

	@JsonIgnore
	public boolean isProfileIDValid() {
		return this.profileID != null && !this.profileID.isBlank();
	}

	@JsonIgnore
	public boolean isCoveredItemsValid() {
		for(String item : this.coveredItems) {
			if (item == null || item.isBlank()) {
				return false;
			}
		}

		return true;
	}

	@JsonIgnore
	public boolean isDomainValid() {
		return this.domain != null && !this.domain.isBlank();
	}

	@JsonIgnore
	public boolean isValid() {
		return this.isProfileIDValid() && this.isProfileNameValid() && this.isCoveredItemsValid() && this.isDomainValid();
	}
}
