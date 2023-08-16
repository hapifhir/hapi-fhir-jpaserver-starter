package ca.uhn.fhir.jpa.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.desair.tus.server.TusFileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Configuration
public class TusServerConfiguration {

	private final TusServerProperties tusServerProperties;

	@Bean
	public TusFileUploadService tusFileUploadService() {
		return new TusFileUploadService()
			.withStoragePath(tusServerProperties.getFileDirectory())
			.withUploadURI(tusServerProperties.getContextPath())
			.withUploadExpirationPeriod(200000L);
	}
}