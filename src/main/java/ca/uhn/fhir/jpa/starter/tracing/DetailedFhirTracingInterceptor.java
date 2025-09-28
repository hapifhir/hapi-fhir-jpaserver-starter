package ca.uhn.fhir.jpa.starter.tracing;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Interceptor
public class DetailedFhirTracingInterceptor {

    @Autowired
    private Tracer tracer;

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
    public void incomingRequestStart(RequestDetails requestDetails) {
        if (requestDetails == null) {
            return; // 安全檢查
        }
        
        String operationName = String.format("FHIR %s %s", 
            requestDetails.getRequestType(), 
            requestDetails.getRequestPath());
            
        Span span = tracer.spanBuilder(operationName)
            .setAttribute("http.method", requestDetails.getRequestType().name())
            .setAttribute("http.url", requestDetails.getCompleteUrl())
            .setAttribute("fhir.operation", operationName)
            .startSpan();
            
        // 將 span 存儲到 request context
        requestDetails.setAttribute("main.span", span);
        requestDetails.setAttribute("main.context", Context.current().with(span));
    }

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
    public void incomingRequestEnd(RequestDetails requestDetails) {
        if (requestDetails == null) {
            return;
        }
        
        Span span = (Span) requestDetails.getAttribute("main.span");
        if (span != null) {
            span.setStatus(StatusCode.OK);
            span.end();
        }
    }

    @Hook(Pointcut.SERVER_HANDLE_EXCEPTION)
    public void handleException(RequestDetails requestDetails, Exception exception) {
        if (requestDetails == null || exception == null) {
            return;
        }
        
        Span span = (Span) requestDetails.getAttribute("main.span");
        if (span != null) {
            span.setStatus(StatusCode.ERROR, exception.getMessage());
            span.recordException(exception);
            span.end();
        }
    }

    @Hook(Pointcut.STORAGE_PREACCESS_RESOURCES)
    public void storagePreAccess(RequestDetails requestDetails) {
        if (requestDetails == null) {
            return;
        }
        
        Context parentContext = (Context) requestDetails.getAttribute("main.context");
        Context context = parentContext != null ? parentContext : Context.current();
        
        Span span = tracer.spanBuilder("DAO: Pre-Access Resources")
            .setParent(context)
            .startSpan();
            
        requestDetails.setAttribute("dao.span", span);
    }

    @Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_CREATED)
    public void storagePreCommit(RequestDetails requestDetails) {
        if (requestDetails == null) {
            return;
        }
        
        Context parentContext = (Context) requestDetails.getAttribute("main.context");
        Context context = parentContext != null ? parentContext : Context.current();
        
        Span span = tracer.spanBuilder("DAO: Pre-Commit Resource")
            .setParent(context)
            .startSpan();
            
        requestDetails.setAttribute("precommit.span", span);
    }
}