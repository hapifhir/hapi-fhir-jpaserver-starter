package ch.ahdis.matchbox.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.JsonParser;
import org.hl7.fhir.r5.elementmodel.ParserBase;
import org.hl7.fhir.r5.elementmodel.XmlParser;
import org.hl7.fhir.r5.model.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

/**
 * A wrapper around HTTP servlet requests and responses to handle FHIR requests within HAPI.
 *
 * @see ca.uhn.fhir.rest.server.RestfulServerUtils
 */
public class HttpRequestWrapper {

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final MatchboxEngine defaultEngine;

	// Request properties
	private final byte[] requestBody;
	private final @Nullable Format requestFormat;

	// Response properties
	private final Format responseFormat;

	public HttpRequestWrapper(final HttpServletRequest request,
									  final HttpServletResponse response,
									  final MatchboxEngine defaultEngine) throws IOException {
		this.request = request;
		this.response = response;
		this.defaultEngine = defaultEngine;

		this.requestBody = request.getInputStream().readAllBytes();
		this.requestFormat = this.parseRequestFormat();
		if (this.requestFormat == null && this.request.getContentLength() > 0) {
			throw new InvalidRequestException("No Content-Type header found in request with body");
		}

		this.responseFormat = this.getResponseFormatOrDefault();
	}

	@Nullable
	public Resource parseBodyAsResource() {
		if (this.requestFormat == null) {
			return null;
		}
		final var parser = this.requestFormat.getResourceParser();
		return (Resource) parser.parseResource(new ByteArrayInputStream(this.requestBody));
	}

	@Nullable
	public Element parseBodyAsElement() throws IOException {
		if (this.requestFormat == null) {
			return null;
		}
		final var parser = this.requestFormat.getElementParser(this.defaultEngine.getContext());
		return parser.parseSingle(new ByteArrayInputStream(this.requestBody), null);
	}

	public void writeResponse(final Resource resource) throws IOException {
		this.response.setContentType(this.responseFormat.getContentType());
		this.response.setCharacterEncoding("UTF-8");

		final var parser = this.responseFormat.getResourceParser();
		parser.setPrettyPrint(true);
		parser.encodeResourceToWriter(resource, this.response.getWriter());
	}

	public void writeResponse(final Element element) throws IOException {
		this.response.setContentType(this.responseFormat.getContentType());
		this.response.setCharacterEncoding("UTF-8");

		final var parser = this.responseFormat.getElementParser(this.defaultEngine.getContext());
		parser.compose(element, response.getOutputStream(), org.hl7.fhir.r5.formats.IParser.OutputStyle.PRETTY, null);
	}

	private Format parseRequestFormat() {
		String value = this.request.getContentType();
		if (value != null) {
			if (value.contains(";")) {
				value = value.substring(0, value.indexOf(";"));
			}
			return this.classifyMimeType(value);
		}
		return null;
	}

	@Nullable
	private Format parseResponseFormat() {
		final Set<String> acceptValues = RestfulServerUtils
			.parseAcceptHeaderAndReturnHighestRankedOptions(this.request);
		for (final String acceptValue : acceptValues) {
			final var format = this.classifyMimeType(acceptValue);
			if (format != null) {
				return format;
			}
		}
		return null;
	}

	private Format getResponseFormatOrDefault() {
		final var format = this.parseResponseFormat();
		return (format != null) ? format : Format.JSON;
	}

	@Nullable
	private Format classifyMimeType(final String mimeType) {
		if (Constants.CT_FHIR_XML.equals(mimeType)
			|| Constants.CT_FHIR_XML_NEW.equals(mimeType)
			|| Constants.CT_XML.equals(mimeType)
			|| Constants.FORMAT_XML.equals(mimeType)) {
			return Format.XML;
		} else if (Constants.CT_FHIR_JSON.equals(mimeType)
			|| Constants.CT_FHIR_JSON_NEW.equals(mimeType)
			|| Constants.CT_JSON.equals(mimeType)
			|| Constants.FORMAT_JSON.equals(mimeType)) {
			return Format.JSON;
		}
		return null;
	}

	private enum Format {
		JSON(Constants.CT_FHIR_JSON_NEW),
		XML(Constants.CT_FHIR_XML_NEW);

		private final String contentType;

		Format(String contentType) {
			this.contentType = contentType;
		}

		public String getContentType() {
			return contentType;
		}

		public IParser getResourceParser() {
			return switch (this) {
				case JSON -> FhirContext.forR5Cached().newJsonParser();
				case XML -> FhirContext.forR5Cached().newXmlParser();
			};
		}

		public ParserBase getElementParser(final IWorkerContext context) {
			return switch (this) {
				case JSON -> new JsonParser(context);
				case XML -> new XmlParser(context);
			};
		}
	}
}
