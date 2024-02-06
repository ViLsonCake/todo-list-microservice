package project.vilsoncake.authorizationserver.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import project.vilsoncake.authorizationserver.property.KeycloakProperties;

import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final KeycloakProperties keycloakProperties;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getRealm())
                .grantType(CLIENT_CREDENTIALS)
                .clientId(keycloakProperties.getAdminClientId())
                .clientSecret(keycloakProperties.getAdminClientSecret())
                .build();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(keycloakProperties.getServerUrl())
                .build();
    }
}
