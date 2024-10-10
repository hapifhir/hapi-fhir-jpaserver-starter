package ca.uhn.fhir.jpa.starter.Security;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final JwtConverterProperties properties;
    private final JwtDecoder jwtDecoder;

    public JwtConverter(JwtConverterProperties properties, JwtDecoder jwtDecoder) {
        this.properties = properties;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt).stream())
                .collect(Collectors.toSet());

        System.out.println("Granted Authorities: " + authorities);

        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    public Jwt parseToken(String token) {

        return jwtDecoder.decode(token);
    }

    private String getPrincipalClaimName(Jwt jwt) {

        String claimName = JwtClaimNames.SUB;
        if (properties.getPrincipalAttribute() != null && !properties.getPrincipalAttribute().isEmpty()) {
            claimName = properties.getPrincipalAttribute();
        }
        return jwt.getClaimAsString(claimName);
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return Set.of();
        }

        Object resourceObj = resourceAccess.get(properties.getResourceId());
        if (!(resourceObj instanceof Map)) {
            return Set.of();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceObj;
        Object rolesObj = resource.get("roles");
        if (!(rolesObj instanceof Collection)) {
            return Set.of();
        }

        Collection<String> resourceRoles = (Collection<String>) rolesObj;

        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
