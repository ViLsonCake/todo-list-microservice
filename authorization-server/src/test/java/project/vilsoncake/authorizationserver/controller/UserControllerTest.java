package project.vilsoncake.authorizationserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import project.vilsoncake.authorizationserver.dto.TokenDto;
import project.vilsoncake.authorizationserver.entity.UserEntity;
import project.vilsoncake.authorizationserver.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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

    @BeforeEach
    void initUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("testuser");
        user.setEmail("testuser@gmail.com");
        userRepository.save(user);
    }

    @AfterEach
    void deleteUser() {
        userRepository.deleteAll();
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
        assertEquals(HttpStatus.SC_CREATED, response.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertTrue(response.getResponse().getContentAsString().contains(username));
        assertEquals(usersBeforeRequest.size() + 1, usersAfterRequest.size());
        assertNotNull(userRepository.findByUsernameIgnoreCase("newuser"));
        assertEquals(HttpStatus.SC_OK, tokenResponse.getResponse().getStatus());
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
        assertEquals(HttpStatus.SC_CONFLICT, response.getResponse().getStatus());
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
        assertEquals(HttpStatus.SC_CONFLICT, response.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertEquals(usersBeforeRequest.size(), usersAfterRequest.size());
    }

    @Test
    @DisplayName("Change user username test with valid new username")
    void changeUserUsername_validUserData() throws Exception {
        String username = "currentUsername";
        String newUsername = "newUsername";
        String email = "changeuser@gmail.com";
        String password = "testpass";
        String createUserRequest = String.format("{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", username, email, password);
        String tokenRequest = "{\"username\":\"%s\",\"password\":\"%s\"}";

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserRequest)
        ).andReturn();

        var tokenResponse = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(tokenRequest, username, password))
        ).andReturn();

        String accessToken = objectMapper.readValue(tokenResponse.getResponse().getContentAsString(), TokenDto.class).getAccessToken();
        String jsonChangeUsernameRequest = String.format("{\"newUsername\":\"%s\"}", newUsername);

        var changeUserResponse = mockMvc.perform(
                patch("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeUsernameRequest)
        ).andReturn();

        var changedUsernameTokenResponse = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(tokenRequest, newUsername, password))
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_OK, changeUserResponse.getResponse().getStatus());
        assertEquals(HttpStatus.SC_OK, changedUsernameTokenResponse.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertNull(userRepository.findByUsernameIgnoreCase(username));
        assertNotNull(userRepository.findByUsernameIgnoreCase(newUsername));
    }

    @Test
    @DisplayName("Change user username test with already used new username")
    void changeUserUsername_invalidUserData() throws Exception {
        String username = "username";
        String newUsedUsername = "testuser";
        String email = "changeuserinvalid@gmail.com";
        String password = "testpass";
        String createUserRequest = String.format("{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", username, email, password);
        String tokenRequest = "{\"username\":\"%s\",\"password\":\"%s\"}";

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserRequest)
        ).andReturn();

        var tokenResponse = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(tokenRequest, username, password))
        ).andReturn();

        String accessToken = objectMapper.readValue(tokenResponse.getResponse().getContentAsString(), TokenDto.class).getAccessToken();
        String jsonChangeUsernameRequest = String.format("{\"newUsername\":\"%s\"}", newUsedUsername);

        var changeUserResponse = mockMvc.perform(
                patch("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeUsernameRequest)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_CONFLICT, changeUserResponse.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertNotNull(userRepository.findByUsernameIgnoreCase(username));
    }

    @Test
    @DisplayName("Remove user test")
    void removeUser() throws Exception {
        String username = "removeUser";
        String email = "removeuser@gmail.com";
        String password = "testpass";
        String createUserRequest = String.format("{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", username, email, password);
        String tokenRequest = "{\"username\":\"%s\",\"password\":\"%s\"}";

        var response = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserRequest)
        ).andReturn();

        var tokenResponse = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(tokenRequest, username, password))
        ).andReturn();

        String accessToken = objectMapper.readValue(tokenResponse.getResponse().getContentAsString(), TokenDto.class).getAccessToken();

        var removeUserResponse = mockMvc.perform(
                delete("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_OK, removeUserResponse.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertNull(userRepository.findByUsernameIgnoreCase(username));
    }
}