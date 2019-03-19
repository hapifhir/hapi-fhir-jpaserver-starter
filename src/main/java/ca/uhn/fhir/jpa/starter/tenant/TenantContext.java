package ca.uhn.fhir.jpa.starter.tenant;

/**
 * @see https://github.com/singram/spring-boot-multitenant
 */
public class TenantContext {

	final public static String DEFAULT_TENANT = "test";

	private static ThreadLocal<String> currentTenant = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return DEFAULT_TENANT;
		}
	};

	public static void setCurrentTenant(String tenant) {
		currentTenant.set(tenant);
	}

	public static String getCurrentTenant() {
		return currentTenant.get();
	}

	public static void clear() {
		currentTenant.remove();
	}

}
