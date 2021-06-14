package ca.uhn.fhir.jpa.starter;
import java.nio.charset.StandardCharsets;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.parser.IParser;
import io.nats.client.Connection;
import io.nats.client.Nats;

@Interceptor
public class NatsInterceptor {
    private final Logger logger = LoggerFactory.getLogger(NatsInterceptor.class);
    private FhirContext ctx = FhirContext.forR4();
    private IParser parser = ctx.newJsonParser();
    private Connection connection;

    public NatsInterceptor() {
        try {
            connection = Nats.connect();
            logger.info("Successfully connected to NATS");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public NatsInterceptor(String connectionString) {
        try {
            connection = Nats.connect(connectionString);
            logger.info("Successfully connected to NATS: {}", connectionString);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void handleEvent(IBaseResource resource, String subjectPrefix){
        String subject = String.format("%s.%s", subjectPrefix, resource.fhirType());
        String data = parser.encodeResourceToString(resource);
        connection.publish(subject, data.getBytes(StandardCharsets.UTF_8));
        logger.info("Published message {}", subject);
    }
    private void handleEvent(IBaseResource oldResource, IBaseResource newResource, String subjectPrefix){
        String subject = String.format("%s.%s", subjectPrefix, oldResource.fhirType());
        String oldData = parser.encodeResourceToString(oldResource);
        String newData = parser.encodeResourceToString(newResource);
        String data = String.format("{\"old\": %s, \"new\": %s}", oldData, newData);
        connection.publish(subject, data.getBytes(StandardCharsets.UTF_8));
        logger.info("Published message {}", subject);
    }

    @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
    public void handleStoragePreStorageCreate(IBaseResource resource){
        handleEvent(resource, "fhir.prestorage.create");
    }

    @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
    public void handleStoragePreStorageUpdate(IBaseResource oldResource, IBaseResource newResource){
        handleEvent(oldResource, newResource, "fhir.prestorage.update");
    }

    @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_DELETED)
    public void handleStoragePreStorageDelete(IBaseResource resource){
        handleEvent(resource, "fhir.prestorage.delete");
    }

    @Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_CREATED)
    public void handleStoragePrecommitCreate(IBaseResource resource){
        handleEvent(resource, "fhir.poststorage.create");
    }

    @Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_UPDATED)
    public void handleStoragePrecommitUpdate(IBaseResource oldResource, IBaseResource newResource){
        handleEvent(oldResource, newResource, "fhir.poststorage.update");
    }

    @Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_DELETED)
    public void handleStoragePrecommitDelete(IBaseResource resource){
        handleEvent(resource, "fhir.poststorage.delete");
    }
    
}
