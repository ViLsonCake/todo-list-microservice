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
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import project.vilsoncake.authorizationserver.entity.UserEntity;
import project.vilsoncake.authorizationserver.repository.UserRepository;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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
        String username = "newuser";
        String password = "testpass";
        String createUserRequest = String.format("{\"username\":\"%s\",\"email\":\"example@gmail.com\",\"password\":\"%s\"}", username, password);
        String tokenRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        List<UserEntity> usersBeforeRequest = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserRequest)
        ).andReturn();

        var tokenResponse = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest)
        ).andReturn();

        List<UserEntity> usersAfterRequest = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();

        // When
        assertEquals(response.getResponse().getStatus(), HttpStatus.SC_CREATED);
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertTrue(response.getResponse().getContentAsString().contains(username));
        assertEquals(usersAfterRequest.size(), usersBeforeRequest.size() + 1);
        assertNotNull(userRepository.findByUsernameIgnoreCase("newuser"));
        assertEquals(tokenResponse.getResponse().getStatus(), HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("Create user test with conflict username")
    void createUser_withConflictUsername() throws Exception {
        String jsonRequest = "{\"username\":\"testuser\",\"email\":\"example@gmail.com\",\"password\":\"testpass\"}";
        List<UserEntity> usersBeforeRequest = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        List<UserEntity> usersAfterRequest = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();

        // When
        assertEquals(response.getResponse().getStatus(), HttpStatus.SC_CONFLICT);
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertEquals(usersBeforeRequest.size(), usersAfterRequest.size());
    }

    @Test
    @DisplayName("Create user test with conflict email")
    void createUser_withConflictEmail() throws Exception {
        String jsonRequest = "{\"username\":\"test\",\"email\":\"testuser@gmail.com\",\"password\":\"testpass\"}";
        List<UserEntity> usersBeforeRequest = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        ).andReturn();

        List<UserEntity> usersAfterRequest = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();

        // When
        assertEquals(response.getResponse().getStatus(), HttpStatus.SC_CONFLICT);
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertEquals(usersBeforeRequest.size(), usersAfterRequest.size());
    }
}