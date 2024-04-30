package project.vilsoncake.authorizationserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import project.vilsoncake.authorizationserver.constant.PatternConst;
import project.vilsoncake.authorizationserver.dto.TokenDto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:22.0.5")
            .withRealmImportFile("keycloak/realm-export.json");
    static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest");

    @BeforeAll
    static void startContainers() {
        keycloakContainer.start();
        postgresqlContainer.start();
    }

    @AfterAll
    static void stopContainers() {
        keycloakContainer.stop();
        postgresqlContainer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () ->
                keycloakContainer.getAuthServerUrl() + "/realms/todo-realm");
        registry.add("keycloak.server-url", keycloakContainer::getAuthServerUrl);
    }

    @Test
    @DisplayName("Login user test with valid user credentials")
    void loginTestUser_validCredentials() throws Exception {
        String jsonRequest = "{\"username\":\"testuser\",\"password\":\"janepass\"}";

        var response = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        String resultJson = response.getResponse().getContentAsString();

        Pattern pattern = Pattern.compile(PatternConst.ACCESS_TOKEN_PATTERN);
        Matcher matcher = pattern.matcher(resultJson);

        // When
        assertEquals(response.getResponse().getStatus(), HttpStatus.OK.value());
        assertNotNull(response.getResponse().getCookie("refresh_token"));
        assertTrue(response.getResponse().getCookie("refresh_token").isHttpOnly());
        assertTrue(matcher.find());
    }

    @Test
    @DisplayName("Login user test with invalid user credentials")
    void loginTestUser_invalidCredentials() throws Exception {
        String jsonRequest = "{\"username\":\"user\",\"password\":\"janepass\"}";

        var response = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        // When
        assertEquals(response.getResponse().getStatus(), HttpStatus.UNAUTHORIZED.value());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }

    @Test
    @DisplayName("Refresh token test with valid tokens")
    void refreshToken_withValidTokens() throws Exception {
        String jsonRequest = "{\"username\":\"testuser\",\"password\":\"janepass\"}";

        // Getting tokens
        var response = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        String accessToken = objectMapper.readValue(response.getResponse().getContentAsString(), TokenDto.class).getAccessToken();
        String refreshToken = response.getResponse().getCookie("refresh_token").getValue();

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);

        // Refresh them
        var refreshResponse = mockMvc.perform(
                post("/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(refreshTokenCookie)
        ).andReturn();

        String resultJson = refreshResponse.getResponse().getContentAsString();

        Pattern pattern = Pattern.compile(PatternConst.ACCESS_TOKEN_PATTERN);
        Matcher matcher = pattern.matcher(resultJson);

        // When
        assertEquals(refreshResponse.getResponse().getStatus(), HttpStatus.OK.value());
        assertNotNull(refreshResponse.getResponse().getCookie("refresh_token"));
        assertTrue(refreshResponse.getResponse().getCookie("refresh_token").isHttpOnly());
        assertTrue(matcher.find());
    }

    @Test
    @DisplayName("Refresh token test with invalid access token")
    void refreshToken_withInValidAccessToken() throws Exception {
        String jsonRequest = "{\"username\":\"testuser\",\"password\":\"janepass\"}";

        // Getting tokens
        var response = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        String accessToken = objectMapper.readValue(response.getResponse().getContentAsString(), TokenDto.class).getAccessToken();
        String refreshToken = response.getResponse().getCookie("refresh_token").getValue();

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);

        // Refresh them
        var refreshResponse = mockMvc.perform(
                post("/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + "sdf" + accessToken)
                        .cookie(refreshTokenCookie)
        ).andReturn();

        // When
        assertEquals(refreshResponse.getResponse().getStatus(), HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Refresh token test with invalid refresh token")
    void refreshToken_withinValidRefreshToken() throws Exception {
        String jsonRequest = "{\"username\":\"testuser\",\"password\":\"janepass\"}";

        // Getting tokens
        var response = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        String accessToken = objectMapper.readValue(response.getResponse().getContentAsString(), TokenDto.class).getAccessToken();
        String refreshToken = response.getResponse().getCookie("refresh_token").getValue();

        Cookie refreshTokenCookie = new Cookie("refresh_token", "sdf" + refreshToken);
        refreshTokenCookie.setHttpOnly(true);

        // Refresh them
        var refreshResponse = mockMvc.perform(
                post("/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(refreshTokenCookie)
        ).andReturn();

        // When
        assertEquals(refreshResponse.getResponse().getStatus(), HttpStatus.UNAUTHORIZED.value());
        assertTrue(refreshResponse.getResponse().getContentAsString().contains("\"message\":"));
    }
}