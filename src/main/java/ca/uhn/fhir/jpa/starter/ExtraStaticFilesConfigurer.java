package ca.uhn.fhir.jpa.starter;

import org.springframework.beans.factory.annotation.Autowired;
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

    public static final String ROOT_CONTEXT_PATH = "/static";
    @Autowired
    AppProperties appProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry theRegistry) {
        theRegistry.addResourceHandler(ROOT_CONTEXT_PATH + "/**").addResourceLocations(appProperties.getStaticLocation());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String path = URI.create(appProperties.getStaticLocation()).getPath();
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);

        registry.addViewController(ROOT_CONTEXT_PATH).setViewName("redirect:" + ROOT_CONTEXT_PATH + "/" + lastSegment + "/index.html");

        registry.addViewController(ROOT_CONTEXT_PATH + "/*").setViewName("redirect:" + ROOT_CONTEXT_PATH + "/" + lastSegment + "/index.html");

        registry.addViewController(ROOT_CONTEXT_PATH + "/" + lastSegment + "/").setViewName("redirect:" + ROOT_CONTEXT_PATH + "/" + lastSegment + "/index.html");

        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

}