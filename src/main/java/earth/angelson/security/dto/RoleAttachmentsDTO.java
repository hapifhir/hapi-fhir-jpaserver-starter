package earth.angelson.security.dto;

import java.util.Map;
import java.util.Set;

public class RoleAttachmentsDTO {
	private Set<RoleWithRuleDTO> roles;
	private Set<Map<String, String>> attachments;

	public RoleAttachmentsDTO() {
	}

	public RoleAttachmentsDTO(Set<RoleWithRuleDTO> roles, Set<Map<String, String>> attachments) {
		this.roles = roles;
		this.attachments = attachments;
	}

	public Set<RoleWithRuleDTO> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleWithRuleDTO> roles) {
		this.roles = roles;
	}

	public Set<Map<String, String>> getAttachments() {
		return attachments;
	}

	public void setAttachments(Set<Map<String, String>> attachments) {
		this.attachments = attachments;
	}
}
