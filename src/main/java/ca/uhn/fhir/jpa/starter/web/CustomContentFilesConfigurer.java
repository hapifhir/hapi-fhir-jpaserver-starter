package ca.uhn.fhir.jpa.starter.web;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileUrlResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.MalformedURLException;

@Configuration
@ConditionalOnProperty(prefix = "hapi.fhir", name = "custom_content_path")
public class CustomContentFilesConfigurer implements WebMvcConfigurer {

	public static final String CUSTOM_CONTENT = "/content";
	private String customContentPath;


	public CustomContentFilesConfigurer(AppProperties appProperties) {
		customContentPath = appProperties.getCustom_content_path();
		if (customContentPath.endsWith("/"))
			customContentPath = customContentPath.substring(0, customContentPath.lastIndexOf('/'));

	}


	@Override
	public void addResourceHandlers(@NotNull ResourceHandlerRegistry theRegistry) {
		if (!theRegistry.hasMappingForPattern(CUSTOM_CONTENT + "/**")) {

			try {
				theRegistry.addResourceHandler(CUSTOM_CONTENT + "/**").addResourceLocations(new FileUrlResource(customContentPath));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}