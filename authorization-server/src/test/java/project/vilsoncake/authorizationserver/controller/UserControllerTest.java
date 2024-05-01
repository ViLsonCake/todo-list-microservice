package project.vilsoncake.authorizationserver.controller;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("Create user test with valid user data")
    void createUser_withValidUserData() throws Exception {
        String jsonRequest = "{\"username\":\"newuser\",\"email\":\"example@gmail.com\",\"password\":\"testpass\"}";

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        // When
        assertEquals(response.getResponse().getStatus(), HttpStatus.CREATED.value());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }

    @Test
    @DisplayName("Create user test with conflict username")
    void createUser_withConflictUsername() throws Exception {
        String jsonRequest = "{\"username\":\"testuser\",\"email\":\"example@gmail.com\",\"password\":\"testpass\"}";

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        // When
        assertEquals(response.getResponse().getStatus(), HttpStatus.CONFLICT.value());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }

    @Test
    @DisplayName("Create user test with conflict email")
    void createUser_withConflictEmail() throws Exception {
        String jsonRequest = "{\"username\":\"test\",\"email\":\"testuser@gmail.com\",\"password\":\"testpass\"}";

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        // When
        assertEquals(response.getResponse().getStatus(), HttpStatus.CONFLICT.value());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }
}