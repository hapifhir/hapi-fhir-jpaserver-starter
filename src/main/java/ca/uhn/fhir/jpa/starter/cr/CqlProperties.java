package ca.uhn.fhir.jpa.starter.cr;

import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings;
import org.opencds.cqf.fhir.cql.engine.terminology.TerminologySettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hapi.fhir.cr.cql")
public class CqlProperties {

	private Boolean use_embedded_libraries = true;
	private CqlCompilerProperties compiler = new CqlCompilerProperties();
	private CqlRuntimeProperties runtime = new CqlRuntimeProperties();
	private TerminologySettings terminology = new TerminologySettings();
	private CqlData data = new CqlData();

	public Boolean getUse_embedded_libraries() {
		return use_embedded_libraries;
	}

	public void setUse_embedded_libraries(Boolean use_embedded_libraries) {
		this.use_embedded_libraries = use_embedded_libraries;
	}

	public CqlCompilerProperties getCompiler() {
		return compiler;
	}

	public void setCompiler(CqlCompilerProperties compiler) {
		this.compiler = compiler;
	}

	public CqlRuntimeProperties getRuntime() {
		return runtime;
	}

	public void setRuntime(CqlRuntimeProperties runtime) {
		this.runtime = runtime;
	}

	public TerminologySettings getTerminology() {
		return terminology;
	}

	public void setTerminology(TerminologySettings terminology) {
		this.terminology = terminology;
	}

	public CqlData getData() {
		return data;
	}

	public void setData(CqlData data) {
		this.data = data;
	}

	public RetrieveSettings getRetrieveSettings() {
		return data.getRetrieveSettings();
	}
}
