//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.hl7.fhir.r5.context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.context.FhirContext;
import lombok.Generated;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.MarkedToMoveToAdjunctPackage;
import org.hl7.fhir.utilities.ToolingClientLogger;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.filesystem.ManagedFileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MarkedToMoveToAdjunctPackage
public class HTMLClientLogger extends BaseLogger implements ToolingClientLogger {
	@Generated
	private static final Logger log = LoggerFactory.getLogger(HTMLClientLogger.class);
	private boolean req = false;
	private PrintStream file;
	private final FhirContext fhirContext = FhirContext.forR4Cached();

	public HTMLClientLogger(String log) throws IOException {
		if (log != null) {
			this.file = new PrintStream(ManagedFileAccess.outStream(log));
			this.file.println(
"""
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.30.0/prism.min.js" integrity="sha512-HiD3V4nv8fcjtouznjT9TqDNDm1EXngV331YGbfVGeKUoH+OLkRTCMzA34ecjlgSQZpdHZupdSrqHY+Hz3l6uQ==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.30.0/components/prism-json.min.js" integrity="sha512-QXFMVAusM85vUYDaNgcYeU3rzSlc+bTV4JvkfJhjxSHlQEo+ig53BtnGkvFTiNJh8D+wv6uWAQ2vJaVmxe8d3w==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.30.0/components/prism-http.min.js" integrity="sha512-3KphgbiKTzK2CNxlSgUKypipTV7tWknO5czNb+E7H4CeHOOSer2s2rIOCTuz8NsY1zm+B9tP9Ul2JX/tmdyOYg==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.30.0/themes/prism.min.css" integrity="sha512-tN7Ec6zAFaVSG3TpNAKtk4DOHNpSwKHxxrsiw4GHKESGPs5njn/0sMCUMl2svV4wo4BK/rCP7juYz+zx+l6oeQ==" crossorigin="anonymous" referrerpolicy="no-referrer" />
<script>
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('code.language-json').forEach((block) => {
    	const json = block.textContent.trim();
    	console.log("JSON length", json.length);
    	if (!json.startsWith("{\\n") && json.length < 3000) {
			try {
				const parsed = JSON.parse(block.textContent);
				block.textContent = "\\n" + JSON.stringify(parsed, null, 3);
			} catch (e) {
				console.error('Failed to parse JSON:', e);
			}
		}
	});
	document.querySelectorAll('pre code').forEach((block) => {
		Prism.highlightElement(block);
	});
	
	document.body.prepend(document.createElement('hr'));
	const summaries = document.createElement('div');
	summaries.innerHTML = Array.from(document.querySelectorAll('span.summary'))
		.map(e => e.innerHTML)
		.join('<br>');
	document.body.prepend(summaries);
	const summaryTitle = document.createElement('h2');
	summaryTitle.innerText = "Summary of requests";
	document.body.prepend(summaryTitle);
});
</script>
<style>
pre { padding: 3px 1em !important; margin: 0 !important; }
pre code { font-size: 13px !important; line-height: 1.3 !important; }
.summary { display: none; }
</style>
""");
		}

	}

	public void logRequest(String method, String url, List<String> headers, byte[] body) {
		log.debug(" txlog req: " + method + " " + url + " " + this.present(body));
		if (this.file != null) {
			String id = this.nextId();
			this.file.println("<hr/>");
			this.file.printf("<a id=\"l%s\">#%s</a>%n", id, id);
			this.file.print("<pre><code class=\"language-http\">");
			this.file.println(method + " " + url + " HTTP/1.0");
			if (headers != null) {
				for(String s : headers) {
					this.file.println(Utilities.escapeXmlText(s));
				}
			}
			this.file.println("</code></pre>");

			if (body != null) {
				if (body.length > 100000) {
					body = Arrays.copyOf(body, 100000);
				}
				this.file.print("<pre><code class=\"language-json\">");
				this.file.println(Utilities.escapeXmlText(new String(body, StandardCharsets.UTF_8)));
				this.file.println("</code></pre>");

				try {
					if (url.contains("/ValueSet/$validate-code?")) {
						this.debugVSValidateCode(body, id);
					} else if (url.contains("/CodeSystem/$validate-code?")) {
						this.debugCSValidateCode(body, id);
					} else {
						log.warn("QLDBG: not analyzed [" + url + "]");
					}
				} catch (final Exception e) {
					log.error("Error analyzing TX request", e);
				}
			} else {
				if (url.endsWith("/metadata")) {
					this.file.printf("""
							<span class="summary"><a href="#l%s">#%s</a> <strong>CapabilityStatement</strong></span>
							""", id, id);
				} else if (url.endsWith("/metadata?mode=terminology")) {
					this.file.printf("""
							<span class="summary"><a href="#l%s">#%s</a> <strong>TerminologyCapabilities</strong></span>
							""",id, id);
				} else if (url.contains("/CodeSystem?")) {
					this.file.printf("""
							<span class="summary"><a href="#l%s">#%s</a> <strong>CodeSystem search</strong></span>
							""", id, id);
				}
			}
			
			this.req = true;
		}
	}

	public void logResponse(String outcome, List<String> headers, byte[] body, long start) {
		log.debug(" txlog resp: " + outcome + " " + this.present(body));
		if (this.file != null) {
			if (!this.req) {
				log.info("Record Response without request");
			}

			this.req = false;
			this.file.print("<br><pre><code class=\"language-http\">");
			this.file.println(outcome);

			for(String s : headers) {
				this.file.println(Utilities.escapeXml(s));
			}
			this.file.println("</code></pre>");

			if (body != null) {
				this.file.print("<pre><code class=\"language-json\">");
				this.file.println(Utilities.escapeXml(new String(body, StandardCharsets.UTF_8)));
				this.file.println("</code></pre>");
			}
		}
	}

	private String present(byte[] body) {
		if (body == null) {
			return "";
		} else {
			String cnt = new String(body);
			cnt = cnt.replace("\n", " ").replace("\r", "");
			return cnt.length() > 800 ? cnt.substring(0, 798) + "..." : cnt;
		}
	}
	
	private void debugVSValidateCode(final byte[] body, final String id) {
		final var params = this.fhirContext.newJsonParser().parseResource(Parameters.class,
																		  new ByteArrayInputStream(body));
		final var url = Optional.ofNullable(params.getParameterValue("url"))
			.map(Object::toString)
			.orElse(Optional.ofNullable(params.getParameter("valueSet"))
						.map(Parameters.ParametersParameterComponent::getResource)
						.map(ValueSet.class::cast)
						.map(ValueSet::getUrl)
						.orElse("unknown"));
		
		if (params.hasParameter("coding")) {
			final var coding = (Coding) params.getParameterValue("coding");
			this.file.printf("""
					  <span class="summary">
						<a href="#l%s">#%s</a>
						<strong>ValueSet $validate-code</strong>
						%s#%s in %s
					  </span>""", id, id, coding.getSystem(), coding.getCode(), url);
		} else if (params.hasParameter("codeableConcept")) {
			final var coding = ((CodeableConcept) params.getParameterValue("codeableConcept")).getCoding().getFirst();
			this.file.printf("""
					  <span class="summary">
						<a href="#l%s">#%s</a>
						<strong>ValueSet $validate-code</strong>
						%s#%s in %s
					  </span>""", id, id, coding.getSystem(), coding.getCode(), url);
		} else if (params.hasParameter("code")) {
			final var code = (CodeType) params.getParameterValue("code");
			this.file.println("""
								  <span class="summary">
								  	<a href="#l%s">#%s</a>
								  	<strong>ValueSet $validate-code</strong>
								  	%s in %s
								  </span>""".formatted(id, id, code.getCode(), url));
		} else {
			log.warn("No code, coding or codeableConcept in $validate-code");
		}
	}
	
	private void debugCSValidateCode(final byte[] body, final String id) {
		final var params = this.fhirContext.newJsonParser().parseResource(Parameters.class,
																		  new ByteArrayInputStream(body));
		var coding = (Coding) params.getParameterValue("coding");
		if (coding == null) {
			coding = ((CodeableConcept) params.getParameterValue("coding")).getCodingFirstRep();
		}
		this.file.println("""
			<span class="summary">
				<a href="#l%s">#%s</a>
				<strong>CodeSystem $validate-code</strong>
				%s#%s
			</span>""".formatted(id, id, coding.getSystem(), coding.getCode()));
	}
}
