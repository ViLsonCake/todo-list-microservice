package project.vilsoncake.authorizationserver.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String realm;
    private String serverUrl;
    private String adminClientId;
    private String adminClientSecret;
    private String tokenUrl;
}
