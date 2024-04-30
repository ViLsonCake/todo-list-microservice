package project.vilsoncake.authorizationserver.service.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import project.vilsoncake.authorizationserver.dto.KeycloakTokenResponse;
import project.vilsoncake.authorizationserver.dto.LoginDto;
import project.vilsoncake.authorizationserver.dto.TokenDto;
import project.vilsoncake.authorizationserver.exception.InvalidRefreshTokenException;
import project.vilsoncake.authorizationserver.property.KeycloakProperties;
import project.vilsoncake.authorizationserver.service.AuthService;

import static org.keycloak.OAuth2Constants.PASSWORD;
import static org.keycloak.OAuth2Constants.REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakProperties keycloakProperties;
    private final WebClient webClient;

    @Override
    public TokenDto loginUser(LoginDto loginDto, HttpServletResponse response) throws InvalidCredentialsException {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", PASSWORD);
        body.add("client_id", keycloakProperties.getAdminClientId());
        body.add("client_secret", keycloakProperties.getAdminClientSecret());
        body.add("username", loginDto.getUsername());
        body.add("password", loginDto.getPassword());

        try {
            KeycloakTokenResponse tokenResponse = webClient.post()
                    .uri(keycloakProperties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();
            if (tokenResponse == null) {
                throw new RuntimeException("Token response is null");
            }
            Cookie cookie = new Cookie("refresh_token", tokenResponse.getRefreshToken());
            cookie.setMaxAge(daysToSeconds(60));
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return new TokenDto(tokenResponse.getAccessToken());
        } catch (WebClientResponseException e) {
            throw new InvalidCredentialsException("Incorrect username or password");
        }
    }

    @Override
    public TokenDto refreshToken(String refreshToken, HttpServletResponse response) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", REFRESH_TOKEN);
        body.add("client_id", keycloakProperties.getAdminClientId());
        body.add("client_secret", keycloakProperties.getAdminClientSecret());
        body.add("refresh_token", refreshToken);

        try {
            KeycloakTokenResponse tokenResponse = webClient.post()
                    .uri(keycloakProperties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();

            if (tokenResponse == null) {
                throw new RuntimeException("Token response is null");
            }

            Cookie cookie = new Cookie("refresh_token", tokenResponse.getRefreshToken());
            cookie.setMaxAge(daysToSeconds(60));
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return new TokenDto(tokenResponse.getAccessToken());
        } catch (WebClientResponseException e) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }

    private int daysToSeconds(int days) {
        return days * 24 * 60 * 60;
    }
}
