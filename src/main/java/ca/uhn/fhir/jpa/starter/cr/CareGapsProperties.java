package ca.uhn.fhir.jpa.starter.cr;

public class CareGapsProperties {
    private String reporter = "default";
	private String section_author = "default";

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getSection_author() {
        return section_author;
    }

    public void setSection_author(String section_author) {
        this.section_author = section_author;
    }
}
