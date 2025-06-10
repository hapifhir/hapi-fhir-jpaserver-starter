package ca.uhn.fhir.jpa.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "report")
public class ReportProperties {
	private List<String> email;
	private String env;
	private String cron;
	private String emailSubject;
	private String emailAttachmentName;
	private List<String> topLevelOrgId;

	public List<String> getEmail() {
		return email;
	}

	public void setEmail(List<String> email) {
		this.email = email;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailAttachmentName() {
		return emailAttachmentName;
	}

	public void setEmailAttachmentName(String emailAttachmentName) {
		this.emailAttachmentName = emailAttachmentName;
	}

	public List<String> getTopLevelOrgId() {
		return topLevelOrgId;
	}

	public void setTopLevelOrgId(List<String> topLevelOrgId) {
		this.topLevelOrgId = topLevelOrgId;
	}
}