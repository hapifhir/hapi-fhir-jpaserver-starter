import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("HealthCheck URL builder")
class HealthCheckTest {

    private static Function<String, String> envOf(Map<String, String> values) {
        return values::get;
    }

    private static Function<String, String> emptyEnv() {
        return key -> null;
    }

    @Nested
    @DisplayName("Scenario 1: defaults (no context-path or management web base path set)")
    class Defaults {

        @Test
        @DisplayName("uses port 8080 and /actuator/health when no env vars are set")
        void defaults_useServerPort8080AndActuatorRoot() {
            String url = HealthCheck.buildHealthUrl(emptyEnv());
            assertEquals("http://127.0.0.1:8080/actuator/health", url);
        }

        @Test
        @DisplayName("honors an explicit SERVER_PORT override")
        void defaults_explicitServerPortIsHonored() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_PORT", "9090");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:9090/actuator/health", url);
        }

        @Test
        @DisplayName("treats blank env values as unset and falls back to defaults")
        void blankEnvValuesFallBackToDefaults() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_PORT", "  ");
            env.put("MANAGEMENT_ENDPOINTS_WEB_BASE_PATH", "");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:8080/actuator/health", url);
        }
    }

    @Nested
    @DisplayName("Scenario 2: either context-path or management web base path is set")
    class ContextOrBasePathSet {

        @Test
        @DisplayName("applies SERVER_SERVLET_CONTEXT_PATH when management shares the server port")
        void contextPathSet_appliedWhenManagementSharesServerPort() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_SERVLET_CONTEXT_PATH", "/fhir");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:8080/fhir/actuator/health", url);
        }

        @Test
        @DisplayName("normalizes a context-path missing the leading slash and with a trailing slash")
        void contextPathSet_normalizesLeadingAndTrailingSlashes() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_SERVLET_CONTEXT_PATH", "fhir/");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:8080/fhir/actuator/health", url);
        }

        @Test
        @DisplayName("treats a context-path of '/' as empty")
        void contextPathSet_rootSlashTreatedAsEmpty() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_SERVLET_CONTEXT_PATH", "/");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:8080/actuator/health", url);
        }

        @Test
        @DisplayName("MANAGEMENT_ENDPOINTS_WEB_BASE_PATH overrides the default /actuator prefix")
        void actuatorWebBasePathSet_overridesDefaultActuatorPath() {
            Map<String, String> env = new HashMap<>();
            env.put("MANAGEMENT_ENDPOINTS_WEB_BASE_PATH", "/manage");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:8080/manage/health", url);
        }

        @Test
        @DisplayName("combines context-path and actuator web base path when both are set")
        void contextPathAndActuatorBasePathBothSet() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_SERVLET_CONTEXT_PATH", "/fhir");
            env.put("MANAGEMENT_ENDPOINTS_WEB_BASE_PATH", "/manage");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:8080/fhir/manage/health", url);
        }
    }

    @Nested
    @DisplayName("Scenario 3: management server uses a different port")
    class DifferentManagementPort {

        @Test
        @DisplayName("ignores context-path and applies MANAGEMENT_SERVER_BASE_PATH on the management port")
        void differentManagementPort_ignoresContextPathAndUsesManagementBasePath() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_PORT", "8080");
            env.put("MANAGEMENT_SERVER_PORT", "9001");
            env.put("SERVER_SERVLET_CONTEXT_PATH", "/fhir");
            env.put("MANAGEMENT_SERVER_BASE_PATH", "/mgmt");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:9001/mgmt/actuator/health", url);
        }

        @Test
        @DisplayName("omits any prefix when management base path is not set")
        void differentManagementPort_withoutManagementBasePath_hasNoPrefix() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_PORT", "8080");
            env.put("MANAGEMENT_SERVER_PORT", "9001");
            env.put("SERVER_SERVLET_CONTEXT_PATH", "/fhir");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:9001/actuator/health", url);
        }

        @Test
        @DisplayName("still applies context-path when MANAGEMENT_SERVER_PORT explicitly equals SERVER_PORT")
        void sameManagementPortExplicitlySet_appliesContextPath() {
            Map<String, String> env = new HashMap<>();
            env.put("SERVER_PORT", "8080");
            env.put("MANAGEMENT_SERVER_PORT", "8080");
            env.put("SERVER_SERVLET_CONTEXT_PATH", "/fhir");
            env.put("MANAGEMENT_SERVER_BASE_PATH", "/mgmt");
            String url = HealthCheck.buildHealthUrl(envOf(env));
            assertEquals("http://127.0.0.1:8080/fhir/actuator/health", url);
        }
    }
}
