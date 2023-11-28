package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.IResourceProviderFactoryObserver;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;

import java.util.function.Supplier;

public class PostInitProviderRegisterer {
	public PostInitProviderRegisterer(RestfulServer restfulServer, ResourceProviderFactory resourceProviderFactory) {
		resourceProviderFactory.attach(new Observer(restfulServer));
	}

	private class Observer implements IResourceProviderFactoryObserver {
		private RestfulServer restfulServer;

		public Observer(RestfulServer restfulServer) {
			this.restfulServer = restfulServer;
		}

		public void update(Supplier<Object> theSupplier) {
			if (theSupplier == null) {
				return;
			}

			var provider = theSupplier.get();
			if (provider == null) {
				return;
			}

			this.restfulServer.registerProvider(provider);
		}

		public void remove(Supplier<Object> theSupplier) {
			if (theSupplier == null) {
				return;
			}

			var provider = theSupplier.get();
			if (provider == null) {
				return;
			}

			this.restfulServer.unregisterProvider(provider);
		}
	}
}
