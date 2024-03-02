package earth.angelson.security.dto;


import java.util.List;
import java.util.UUID;


public class RuleDTO {

	private UUID id;
	private String name;
	private String operation;
	private List<String> resource;
	private String filter;

	public RuleDTO() {
	}

	public RuleDTO(UUID id, String name, String operation, List<String> resource, String filter) {
		this.id = id;
		this.name = name;
		this.operation = operation;
		this.resource = resource;
		this.filter = filter;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public List<String> getResource() {
		return resource;
	}

	public void setResource(List<String> resource) {
		this.resource = resource;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
}
