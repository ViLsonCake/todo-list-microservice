package project.vilsoncake.todocategoryservice.keycloak;

import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.keycloak.OAuth2Constants.PASSWORD;

@Component
public class KeycloakUtils {

    private final WebClient webClient = WebClient.builder().build();
    private final KeycloakProperties keycloakProperties;

    public KeycloakUtils(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    public KeycloakTokenResponse getTestUserAuthTokens() throws InvalidCredentialsException {
        String username = "testuser";
        String password = "janepass";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", PASSWORD);
        body.add("client_id", keycloakProperties.getAdminClientId());
        body.add("client_secret", keycloakProperties.getAdminClientSecret());
        body.add("username", username);
        body.add("password", password);

        try {
            return webClient.post()
                    .uri(keycloakProperties.getServerUrl() + keycloakProperties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new InvalidCredentialsException("Incorrect username or password");
        }
    }
}
