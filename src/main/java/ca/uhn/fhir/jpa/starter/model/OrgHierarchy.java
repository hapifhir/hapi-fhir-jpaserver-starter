package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "organization_structure")
public class OrgHierarchy {

	@Id
	@Column(name = "id",  columnDefinition = "VARCHAR(36)")
	private String id;

	@Column(name = "orgId", nullable = false, unique = true)
	private String orgId;

	@Column(name = "level", nullable = false)
	private String level;

	@Column(name = "countryParent", nullable = true)
	private String countryParent;

	@Column(name = "stateParent", nullable = true)
	private String stateParent;

	@Column(name = "lgaParent", nullable = true)
	private String lgaParent;

	@Column(name = "wardParent", nullable = true)
	private String wardParent;


	public OrgHierarchy() {}
	public OrgHierarchy(String orgId, String level, String countryParent, String stateParent, String lgaParent, String wardParent) {
		this.id = UUID.randomUUID().toString();
		this.orgId = orgId;
		this.level = level;
		this.countryParent = countryParent;
		this.stateParent = stateParent;
		this.lgaParent = lgaParent;
		this.wardParent = wardParent;
	}


	public String getOrgId() {
		return orgId;
	}

	public String getId() {
		return id;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getStateParent() {
		return stateParent;
	}

	public void setStateParent(String stateParent) {
		this.stateParent = stateParent;
	}

	public String getLgaParent() {
		return lgaParent;
	}

	public void setLgaParent(String lgaParent) {
		this.lgaParent = lgaParent;
	}

	public String getCountryParent() {
		return countryParent;
	}

	public void setCountryParent(String countryParent) {
		this.countryParent = countryParent;
	}

	public String getWardParent() {
		return wardParent;
	}

	public void setWardParent(String wardParent) {
		this.wardParent = wardParent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(orgId, level, countryParent, stateParent, lgaParent, wardParent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		OrgHierarchy other = (OrgHierarchy) obj;
		return Objects.equals(orgId, other.orgId) &&
			Objects.equals(level, other.level) &&
			Objects.equals(countryParent, other.countryParent) &&
			Objects.equals(stateParent, other.stateParent) &&
			Objects.equals(lgaParent, other.lgaParent) &&
			Objects.equals(wardParent, other.wardParent);
	}

	@Override
	public String toString() {
		// Create a custom string representation for OrgHierarchy
		return "OrgHierarchy{" +
			"orgId='" + orgId + '\'' +
			", level=" + level +
			", countryParent='" + countryParent + '\'' +
			", stateParent='" + stateParent + '\'' +
			", lgaParent='" + lgaParent + '\'' +
			", wardParent='" + wardParent + '\'' +
			'}';
	}

}
