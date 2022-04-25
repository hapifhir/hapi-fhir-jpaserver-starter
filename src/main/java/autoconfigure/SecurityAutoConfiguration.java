package autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for HAPI FHIR.
 *
 */
@Configuration
@AutoConfigureAfter({ KeycloakSecurityConfig.class })
@Import({ KeycloakSecurityConfig.class, DisableSecurityConfig.class })
public class SecurityAutoConfiguration {
}