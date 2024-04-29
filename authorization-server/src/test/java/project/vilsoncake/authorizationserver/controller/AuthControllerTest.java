package project.vilsoncake.authorizationserver.controller;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

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

    static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:22.0.5")
            .withRealmImportFile("keycloak/realm-export.json");
    static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest");

    @Value("${keycloak.realm}")
    static String realmName;

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
                keycloakContainer.getAuthServerUrl() + "/realms/" + realmName);
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

        String regex = "^\\{\"access_token\":\\s*\"[\\w-]+\\.[\\w-]+\\.[\\w-]+\"}$";
        String resultJson = response.getResponse().getContentAsString();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(resultJson);

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

        assertEquals(response.getResponse().getStatus(), HttpStatus.UNAUTHORIZED.value());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }
}