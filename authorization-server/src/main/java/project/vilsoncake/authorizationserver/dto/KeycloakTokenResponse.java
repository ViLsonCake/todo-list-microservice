package project.vilsoncake.authorizationserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class KeycloakTokenResponse {
    @JsonAlias("access_token")
    private String accessToken;
    @JsonAlias("refresh_token")
    private String refreshToken;
}
