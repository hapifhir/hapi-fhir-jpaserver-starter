package ca.uhn.fhir.jpa.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tus-server")
public class TusServerProperties {

	//final storage file directory
	private String fileDirectory;

	//upload url
	private String contextPath;
}
