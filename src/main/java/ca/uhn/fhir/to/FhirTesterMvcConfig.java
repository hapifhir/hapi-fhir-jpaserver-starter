package ca.uhn.fhir.to;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ComponentScan(basePackages = "ca.uhn.fhir.to")
public class FhirTesterMvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry theRegistry) {

		theRegistry.addResourceHandler("/assets/**").addResourceLocations("/assets/");
    theRegistry.addResourceHandler("/assets/i18n/**").addResourceLocations("/assets/i18n");
		theRegistry.addResourceHandler("/**").addResourceLocations("/");
    theRegistry.addResourceHandler("/").addResourceLocations("/index.html");
    theRegistry.addResourceHandler("/home").addResourceLocations("/index.html");
	}

}
