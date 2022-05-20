package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.mdm.api.MdmLinkJson;
import ca.uhn.fhir.mdm.api.MdmMatchResultEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MdmQueryLinks {
	private static final Logger ourLog = LoggerFactory.getLogger(MdmQueryLinks.class);
	static final FhirContext ourFhirContext = FhirContext.forR4Cached();

	public static void main(String[] args) {
		IGenericClient fhirClient = ourFhirContext.newRestfulGenericClient("http://localhost:8080/fhir");

		Parameters result = getMdmLinks(fhirClient);

		printMdmLinks(result);
	}

	private static void printMdmLinks(Parameters result) {
		System.out.format("\n%15s\t%15s\t%15s\n", "Source", "Golden", "Result");
		result.getParameter().stream()
			.filter(parameter -> "link".equals(parameter.getName()))
			.forEach(parameter ->
			{
				MdmLinkJson link = partsToLink(parameter);
				System.out.format("%15s\t%15s\t%15s\n", link.getSourceId(), link.getGoldenResourceId(),
					link.getLinkCreatedNewResource() ? "NEW" : link.getMatchResult());
			});
		System.out.println("");
	}

	private static Parameters getMdmLinks(IGenericClient fhirClient) {
		Parameters result = fhirClient.operation()
			.onServer()
			.named("$mdm-query-links")
			.withNoParameters(Parameters.class)
			.execute();
		return result;
	}

	@NotNull
	private static MdmLinkJson partsToLink(Parameters.ParametersParameterComponent parameter) {
		MdmLinkJson retval = new MdmLinkJson();
		parameter.getPart().forEach(part -> {
			switch(part.getName()) {
				case "sourceResourceId":
					retval.setSourceId(part.getValue().toString());
					break;
				case "goldenResourceId":
					retval.setGoldenResourceId(part.getValue().toString());
					break;
				case "matchResult":
					retval.setMatchResult(MdmMatchResultEnum.valueOf(part.getValue().toString()));
					break;
				case "hadToCreateNewResource":
					BooleanType value = (BooleanType) part.getValue();
					retval.setLinkCreatedNewResource(value.booleanValue());
					break;
			}
		});
		return retval;
	}
}
