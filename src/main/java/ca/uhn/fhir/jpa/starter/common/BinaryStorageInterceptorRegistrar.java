package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.IHapiBootOrder;
import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.jpa.binary.interceptor.BinaryStorageInterceptor;
import ca.uhn.fhir.jpa.starter.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class BinaryStorageInterceptorRegistrar {
	private static final Logger ourLog = LoggerFactory.getLogger(BinaryStorageInterceptorRegistrar.class);

	private final IInterceptorService myInterceptorService;
	private final BinaryStorageInterceptor<?> myBinaryStorageInterceptor;
	private final AppProperties myAppProperties;

	public BinaryStorageInterceptorRegistrar(
			IInterceptorService theInterceptorService,
			BinaryStorageInterceptor<?> theBinaryStorageInterceptor,
			AppProperties theAppProperties) {
		myInterceptorService = theInterceptorService;
		myBinaryStorageInterceptor = theBinaryStorageInterceptor;
		myAppProperties = theAppProperties;
	}

	@EventListener(classes = {ContextRefreshedEvent.class})
	@Order(IHapiBootOrder.REGISTER_INTERCEPTORS)
	public void register() {
		if (!myAppProperties.getBinary_storage_enabled()) {
			ourLog.debug("Binary storage disabled; skipping BinaryStorageInterceptor registration");
			return;
		}
		ourLog.info("Registering BinaryStorageInterceptor with JPA interceptor service");
		myInterceptorService.registerInterceptor(myBinaryStorageInterceptor);
	}
}
