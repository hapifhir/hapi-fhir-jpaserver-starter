package ca.uhn.fhir.jpa.starter.model;

import java.util.ArrayList;
import java.util.List;

public class JWTPayload {
	private float exp;
	private float iat;
	private String jti;
	private String iss;
	private String aud;
	private String sub;
	private String typ;
	private String azp;
	private String session_state;
	private String acr;
	List<String> allowed_origins = new ArrayList<>();
	Realm_access Realm_accessObject;
	Resource_access Resource_accessObject;
	private String scope;
	private String sid;
	private boolean email_verified;
	private String practitioner_role_id;
	private String name;
	private String preferred_username;
	private String given_name;
	private String family_name;
	private String email;

	public float getExp() {
		return exp;
	}

	public void setExp(float exp) {
		this.exp = exp;
	}

	public float getIat() {
		return iat;
	}

	public void setIat(float iat) {
		this.iat = iat;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}

	public String getAud() {
		return aud;
	}

	public void setAud(String aud) {
		this.aud = aud;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getAzp() {
		return azp;
	}

	public void setAzp(String azp) {
		this.azp = azp;
	}

	public String getSession_state() {
		return session_state;
	}

	public void setSession_state(String session_state) {
		this.session_state = session_state;
	}

	public String getAcr() {
		return acr;
	}

	public void setAcr(String acr) {
		this.acr = acr;
	}

	public List<String> getAllowed_origins() {
		return allowed_origins;
	}

	public void setAllowed_origins(List<String> allowed_origins) {
		this.allowed_origins = allowed_origins;
	}

	public Realm_access getRealm_accessObject() {
		return Realm_accessObject;
	}

	public void setRealm_accessObject(Realm_access realm_accessObject) {
		Realm_accessObject = realm_accessObject;
	}

	public Resource_access getResource_accessObject() {
		return Resource_accessObject;
	}

	public void setResource_accessObject(Resource_access resource_accessObject) {
		Resource_accessObject = resource_accessObject;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public boolean isEmail_verified() {
		return email_verified;
	}

	public void setEmail_verified(boolean email_verified) {
		this.email_verified = email_verified;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPreferred_username() {
		return preferred_username;
	}

	public void setPreferred_username(String preferred_username) {
		this.preferred_username = preferred_username;
	}

	public String getGiven_name() {
		return given_name;
	}

	public void setGiven_name(String given_name) {
		this.given_name = given_name;
	}

	public String getFamily_name() {
		return family_name;
	}

	public void setFamily_name(String family_name) {
		this.family_name = family_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPractitioner_role_id(String practitioner_role_id) {
		this.practitioner_role_id = practitioner_role_id;
	}

	public String getPractitionerRoleId() {
		return this.practitioner_role_id;
	}

}
