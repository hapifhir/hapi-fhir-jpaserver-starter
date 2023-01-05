package ca.uhn.fhir.jpa.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(prefix = "hapi.fhir", name = "staticLocation")
public class ExtraStaticFilesConfigurer implements WebMvcConfigurer {

	@Autowired
	AppProperties appProperties;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry theRegistry) {
		theRegistry
			.addResourceHandler("/static/**")
		.addResourceLocations(appProperties.getStaticLocation());
	}
}