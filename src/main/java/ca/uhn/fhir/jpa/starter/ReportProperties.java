package ca.uhn.fhir.jpa.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "report")
public class ReportProperties {
	private String env;
	private String emailAttachmentName;

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getEmailAttachmentName() {
		return emailAttachmentName;
	}

	public void setEmailAttachmentName(String emailAttachmentName) {
		this.emailAttachmentName = emailAttachmentName;
	}
}