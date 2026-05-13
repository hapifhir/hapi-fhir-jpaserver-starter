package ca.uhn.fhir.jpa.starter.web;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.FileUrlResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.MalformedURLException;

@Configuration
@ConditionalOnProperty(prefix = "hapi.fhir", name = "app_content_path")
public class WebAppFilesConfigurer implements WebMvcConfigurer {

	public static final String WEB_CONTENT = "/web/apps";
	private String appContentPath;

	public WebAppFilesConfigurer(AppProperties appProperties) {
		appContentPath = appProperties.getApp_content_path();
		if (appContentPath.endsWith("/")) appContentPath = appContentPath.substring(0, appContentPath.lastIndexOf('/'));
	}

	@Override
	public void addResourceHandlers(@NotNull ResourceHandlerRegistry theRegistry) {
		if (!theRegistry.hasMappingForPattern(WEB_CONTENT + "/**")) {
			{
				try {
					theRegistry
							.addResourceHandler(WEB_CONTENT + "/**")
							.addResourceLocations(new FileUrlResource(appContentPath + "/"));
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void addViewControllers(@NotNull ViewControllerRegistry registry) {
		// Set up redirects for the root web content path to serve index.html
		// /web -> redirect to /web/index.html
		// /web/ -> redirect to index.html
		registry.addViewController(WEB_CONTENT)
				.setViewName("redirect:" + WEB_CONTENT + "/index.html");
		registry.addViewController(WEB_CONTENT + "/").setViewName("redirect:index.html");

		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
	}
}
