package ch.ahdis.matchbox.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ch.ahdis.matchbox.terminology.MatchboxTxServer;
import ch.ahdis.matchbox.terminology.interceptors.TerminologyCapabilitiesInterceptor;
import ch.ahdis.matchbox.terminology.providers.CapabilityStatementProvider;
import ch.ahdis.matchbox.terminology.providers.CodeSystemProvider;
import ch.ahdis.matchbox.terminology.providers.ValueSetProvider;
import ch.ahdis.matchbox.terminology.validation.TxValidationCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MatchboxTxConfig {

	@Bean
	public MatchboxTxServer matchboxTxServer(final FhirContext fhirContext,
														  final TxValidationCache txValidationCache) {
		final var server = new MatchboxTxServer(fhirContext);

		server.setServerAddressStrategy(new IncomingRequestAddressStrategy());
		server.setServerConformanceProvider(new CapabilityStatementProvider(server, fhirContext));
		server.setDefaultPrettyPrint(true);

		server.registerInterceptor(new ResponseHighlighterInterceptor());
		server.registerInterceptor(new TerminologyCapabilitiesInterceptor());

		server.registerProvider(new CodeSystemProvider(fhirContext));
		server.registerProvider(new ValueSetProvider(fhirContext, txValidationCache));

		return server;
	}

	@Bean
	public TxValidationCache txValidationCache() {
		return new TxValidationCache();
	}
}
