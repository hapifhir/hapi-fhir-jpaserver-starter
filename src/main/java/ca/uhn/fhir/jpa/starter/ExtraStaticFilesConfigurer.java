package ca.uhn.fhir.jpa.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;

@Configuration
@ConditionalOnProperty(prefix = "hapi.fhir", name = "staticLocation")
public class ExtraStaticFilesConfigurer implements WebMvcConfigurer {

	private String staticLocation;
	private String rootContextPath;

	public ExtraStaticFilesConfigurer(AppProperties appProperties) {

		rootContextPath = appProperties.getStaticLocationPrefix();
		if(rootContextPath.endsWith("/"))
			rootContextPath = rootContextPath.substring(0, rootContextPath.lastIndexOf('/'));

		staticLocation = appProperties.getStaticLocation();
		if(staticLocation.endsWith("/"))
			staticLocation = staticLocation.substring(0, staticLocation.lastIndexOf('/'));

	}


	@Override
    public void addResourceHandlers(ResourceHandlerRegistry theRegistry) {
        theRegistry.addResourceHandler(rootContextPath + "/**").addResourceLocations(staticLocation);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String path = URI.create(staticLocation).getPath();
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);

        registry.addViewController(rootContextPath).setViewName("redirect:" + rootContextPath + "/" + lastSegment + "/index.html");

        registry.addViewController(rootContextPath + "/*").setViewName("redirect:" + rootContextPath + "/" + lastSegment + "/index.html");

        registry.addViewController(rootContextPath + "/" + lastSegment + "/").setViewName("redirect:" + rootContextPath + "/" + lastSegment + "/index.html");

        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

}