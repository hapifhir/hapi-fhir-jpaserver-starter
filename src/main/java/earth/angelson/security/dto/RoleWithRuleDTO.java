package earth.angelson.security.dto;

import java.util.Set;
import java.util.UUID;

public class RoleWithRuleDTO {
	private UUID id;
	private String roleName;
	private Set<RuleDTO> rules;

	public RoleWithRuleDTO() {
	}

	public RoleWithRuleDTO(UUID id, String roleName, Set<RuleDTO> rules) {
		this.id = id;
		this.roleName = roleName;
		this.rules = rules;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Set<RuleDTO> getRules() {
		return rules;
	}

	public void setRules(Set<RuleDTO> rules) {
		this.rules = rules;
	}
}
