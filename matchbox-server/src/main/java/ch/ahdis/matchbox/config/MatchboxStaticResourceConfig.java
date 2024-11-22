package ch.ahdis.matchbox.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
@Configuration
public class MatchboxStaticResourceConfig implements WebMvcConfigurer {

	// The base path to the static resources. E.g. 'classpath:/static/browser/'
	private final String baseResPath;

	// The path to the index.html file. E.g. '/static/browser/index.html'
	private final String indexHtmlPath;

	// The base server path. E.g. '/matchboxv3'
	private final String baseServerPath;

	public MatchboxStaticResourceConfig(@Value("${spring.web.resources.static-locations}") final String staticLocation,
													@Value("${server.servlet.context-path}") final String contextPath) {
		this.baseResPath = staticLocation;
		this.indexHtmlPath = staticLocation.split(":")[1].substring(staticLocation.contains(":/") ? 1 : 0)
			+ "index.html";
		this.baseServerPath = contextPath;
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler("/**")
			.addResourceLocations(this.baseResPath)
			.setCachePeriod(3600)
			.resourceChain(true)
			.addResolver(new MatchboxEncodedResourceResolver(this))
			.addTransformer(new MatchboxResourceTransformer(this));
	}

	/**
	 * A custom resource resolver to handle Angular routes and redirect them to index.html.
	 */
	static class MatchboxEncodedResourceResolver extends EncodedResourceResolver {
		private final MatchboxStaticResourceConfig parent;

		public MatchboxEncodedResourceResolver(final MatchboxStaticResourceConfig parent) {
			super();
			this.parent = parent;
		}

		@Override
		public Resource resolveResource(final HttpServletRequest request,
												  final String requestPath,
												  final List<? extends Resource> locations,
												  final ResourceResolverChain chain) {
			return switch (requestPath) {
				case "", "mappinglanguage", "CapabilityStatement", "igs", "settings", "transform", "validate":
					// All the Angular routes must be redirected to index.html
					// This must be kept in sync with the routes defined in app.module.ts
					yield new ClassPathResource(this.parent.indexHtmlPath);
				default:
					// Otherwise, use the default resolver
					yield super.resolveResource(request, requestPath, locations, chain);
			};
		}
	}

	/**
	 * A custom resource transformer to inject some variables in the index.html content.
	 */
	static class MatchboxResourceTransformer implements ResourceTransformer {
		private final MatchboxStaticResourceConfig parent;

		public MatchboxResourceTransformer(final MatchboxStaticResourceConfig parent) {
			this.parent = parent;
		}

		@Override
		public Resource transform(final HttpServletRequest request,
										  final Resource resource,
										  final ResourceTransformerChain transformerChain) throws IOException {
			if (resource instanceof final ClassPathResource classPathResource && this.parent.indexHtmlPath.equals(
				classPathResource.getPath())) {
				String content = classPathResource.getContentAsString(StandardCharsets.UTF_8);

				// Replace the base path of the server
				content = content.replace("MATCHBOX_BASE_PATH = \"/\"",
												  "MATCHBOX_BASE_PATH = \"%s\"".formatted(this.parent.baseServerPath));

				return new TransformedResource(resource,
														 content.getBytes(StandardCharsets.UTF_8));
			}

			return resource;
		}
	}
}
