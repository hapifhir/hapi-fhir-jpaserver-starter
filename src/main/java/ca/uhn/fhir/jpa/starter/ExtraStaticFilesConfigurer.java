package ca.uhn.fhir.jpa.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@ConditionalOnProperty(prefix = "hapi.fhir", name = "staticLocation")
public class ExtraStaticFilesConfigurer implements WebMvcConfigurer {

	private String staticLocation;
	private String rootContextPath;

	public ExtraStaticFilesConfigurer(AppProperties appProperties) {

		rootContextPath = appProperties.getStaticLocationPrefix();
		if (rootContextPath.endsWith("/"))
			rootContextPath = rootContextPath.substring(0, rootContextPath.lastIndexOf('/'));


		staticLocation = appProperties.getStaticLocation();
		/*if(staticLocation.endsWith("/"))
			staticLocation = staticLocation.substring(0, staticLocation.lastIndexOf('/'));*/

		System.out.println(new File(".").getAbsolutePath());
	}


	@Override
	public void addResourceHandlers(ResourceHandlerRegistry theRegistry) {

		try {
			theRegistry.addResourceHandler(rootContextPath + "/**").addResourceLocations(new FileUrlResource(staticLocation));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

    /*@Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String path = URI.create(staticLocation).getPath();
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);

        registry.addViewController(rootContextPath).setViewName("redirect:" + rootContextPath + "/" + lastSegment + "/index.html");

        registry.addViewController(rootContextPath + "/*").setViewName("redirect:" + rootContextPath + "/" + lastSegment + "/index.html");

        registry.addViewController(rootContextPath + "/" + lastSegment + "/").setViewName("redirect:" + rootContextPath + "/" + lastSegment + "/index.html");

        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }*/

}