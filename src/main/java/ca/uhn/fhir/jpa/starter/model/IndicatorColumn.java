package ca.uhn.fhir.jpa.starter.model;

public class IndicatorColumn {
	private int id;
	private String name;

	public IndicatorColumn(int i, String name) {
		this.id = i;
		this.name = name;
	}

    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}