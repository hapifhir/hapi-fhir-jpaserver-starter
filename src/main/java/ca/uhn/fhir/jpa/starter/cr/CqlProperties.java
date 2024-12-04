package ca.uhn.fhir.jpa.starter.cr;

import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings;
import org.opencds.cqf.fhir.cql.engine.terminology.TerminologySettings;

public class CqlProperties {

    private Boolean use_embedded_libraries = true;
    private CqlCompilerProperties compiler = new CqlCompilerProperties();
    private CqlRuntimeProperties runtime = new CqlRuntimeProperties();
    private TerminologySettings terminology = new TerminologySettings();
    private RetrieveSettings data = new RetrieveSettings();
    
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

    public RetrieveSettings getData() {
        return data;
    }

    public void setData(RetrieveSettings data) {
        this.data = data;
    }
}
