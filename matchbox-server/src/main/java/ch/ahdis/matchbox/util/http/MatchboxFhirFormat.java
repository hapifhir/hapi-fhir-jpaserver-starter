package ch.ahdis.matchbox.util.http;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.Constants;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.JsonParser;
import org.hl7.fhir.r5.elementmodel.ParserBase;
import org.hl7.fhir.r5.elementmodel.XmlParser;

/**
 * A set of FHIR formats supported by Matchbox.
 *
 * @author Quentin Ligier
 **/
public enum MatchboxFhirFormat {
	JSON(Constants.CT_FHIR_JSON_NEW),
	XML(Constants.CT_FHIR_XML_NEW),
	FML("text/fhir-mapping");

	private final String contentType;

	MatchboxFhirFormat(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public IParser getResourceParser() {
		return switch (this) {
			case JSON -> FhirContext.forR5Cached().newJsonParser();
			case XML -> FhirContext.forR5Cached().newXmlParser();
			case FML -> throw new IllegalStateException("FML is not a parseable format");
		};
	}

	public ParserBase getElementParser(final IWorkerContext context) {
		return switch (this) {
			case JSON -> new JsonParser(context);
			case XML -> new XmlParser(context);
			case FML -> throw new IllegalStateException("FML is not a parseable format");
		};
	}

	public boolean isResourceFormat() {
		return switch (this) {
			case JSON, XML -> true;
			case FML -> false;
		};
	}
}
