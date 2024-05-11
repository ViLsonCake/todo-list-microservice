package project.vilsoncake.todoservice.controller;

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
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;
import project.vilsoncake.todoservice.document.TodoDocument;
import project.vilsoncake.todoservice.dto.TodoDto;
import project.vilsoncake.todoservice.dto.TodosDto;
import project.vilsoncake.todoservice.keycloak.KeycloakUtils;
import project.vilsoncake.todoservice.repository.TodoRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeycloakUtils keycloakUtils;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:22.0.5")
            .withRealmImportFile("keycloak/realm-export.json");

    static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.11.1")
            .withEnv("discovery.type", "single-node");

    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEmbeddedZookeeper();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloakContainer.getAuthServerUrl() + "/realms/todo-realm");
        registry.add("keycloak.server-url", keycloakContainer::getAuthServerUrl);
        registry.add("elastic.host", () -> elasticsearchContainer.getHttpHostAddress().split(":")[0]);
        registry.add("elastic.port", () -> elasticsearchContainer.getHttpHostAddress().split(":")[1]);
    }

    @BeforeAll
    static void startContainers() {
        keycloakContainer.start();
        elasticsearchContainer.start();
        kafkaContainer.start();
    }

    @AfterAll
    static void stopContainers() {
        keycloakContainer.stop();
        elasticsearchContainer.stop();
        kafkaContainer.stop();
    }

    @BeforeEach
    void addInitTodos() {
        TodoDocument todo1 = new TodoDocument();
        todo1.setId(UUID.randomUUID());
        todo1.setTitle("Init todo");
        todo1.setCategory("Home");
        todo1.setText("Init todo text");
        todo1.setCreatedAt(new Date());
        todo1.setCompleted(false);
        todo1.setOwner("testuser");

        TodoDocument todo2 = new TodoDocument();
        todo2.setId(UUID.randomUUID());
        todo2.setTitle("Second init todo");
        todo2.setCategory("Home");
        todo2.setText("Second init todo text");
        todo2.setCreatedAt(new Date());
        todo2.setCompleted(true);
        todo2.setOwner("testuser");

        TodoDocument todo3 = new TodoDocument();
        todo2.setId(UUID.randomUUID());
        todo2.setTitle("Third init todo");
        todo2.setCategory("Home");
        todo2.setText("Third init todo text");
        todo2.setCreatedAt(new Date());
        todo2.setCompleted(true);
        todo2.setOwner("testuser");

        TodoDocument todo4 = new TodoDocument();
        todo2.setId(UUID.randomUUID());
        todo2.setTitle("Fourth init todo");
        todo2.setCategory("Work");
        todo2.setText("Fourth init todo text");
        todo2.setCreatedAt(new Date());
        todo2.setCompleted(true);
        todo2.setOwner("testuser");

        todoRepository.saveAll(List.of(todo1, todo2, todo3, todo4));
    }

    @AfterEach
    void removeInitTodos() {
        todoRepository.deleteAll();
    }

    @Test
    @DisplayName("Create new todo test with valid data")
    void addTodo_validData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String username = "testuser";
        String title = "test todo";
        String jsonTodoRequest = String.format("""
                {
                  "title": "%s",
                  "category": "Home",
                  "text": "Test todo."
                }
                """, title);

        var response = mockMvc.perform(
                post("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTodoRequest)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_CREATED, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertTrue(todoRepository.findByTitleIgnoreCaseAndOwnerIgnoreCase(title, username).isPresent());
    }

    @Test
    @DisplayName("Create new todo test with invalid data")
    void addTodo_invalidData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String jsonTodoRequest = """
                {
                  "title": "",
                  "category": "Home",
                  "text": ""
                }
                """;

        var response = mockMvc.perform(
                post("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTodoRequest)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getResponse().getStatus());
    }

    @Test
    @DisplayName("Change todo completed field test with valid todo title")
    void changeCompleted_validData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();

        var response = mockMvc.perform(
                patch("/todos")
                        .param("title", "Init todo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();

        TodoDto todoDto = objectMapper.readValue(response.getResponse().getContentAsString(), TodoDto.class);

        // When
        assertEquals(HttpStatus.SC_OK, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(todoDto.isCompleted());
    }

    @Test
    @DisplayName("Change todo completed field test with invalid todo title")
    void changeCompleted_invalidData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();

        var response = mockMvc.perform(
                patch("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("title", "non-existing title")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }

    @Test
    @DisplayName("Remove todo test with valid title name")
    void removeTodo_validData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String username = "testuser";
        String title = "Todo for remove";
        String jsonTodoRequest = String.format("""
                {
                  "title": "%s",
                  "category": "Home",
                  "text": "Todo for remove"
                }
                """, title);

        mockMvc.perform(
                post("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTodoRequest)
        ).andReturn();

        var removeTodoResponse = mockMvc.perform(
                delete("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("title", title)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_OK, removeTodoResponse.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, removeTodoResponse.getResponse().getContentType());
        assertTrue(removeTodoResponse.getResponse().getContentAsString().contains("\"message\":"));
        assertTrue(todoRepository.findByTitleIgnoreCaseAndOwnerIgnoreCase(title, username).isEmpty());
    }

    @Test
    @DisplayName("Remove todo test with invalid title name")
    void removeTodo_invalidData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();

        var response = mockMvc.perform(
                delete("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("title", "non-existing title")
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }

    @Test
    @DisplayName("Get all user todos test with default filter")
    void getAllUserTodos_defaultFilter() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String username = "testuser";

        var response = mockMvc.perform(
                get("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        ).andReturn();

        TodosDto todosDto = objectMapper.readValue(response.getResponse().getContentAsString(), TodosDto.class);

        // When
        assertEquals(HttpStatus.SC_OK, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertEquals(todoRepository.findAllByOwnerIgnoreCase(username).size(), todosDto.getTodos().size());
    }

    @Test
    @DisplayName("Get all user todos test with only-completed filter")
    void getAllUserTodos_withOnlyCompletedFilter() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();

        var response = mockMvc.perform(
                get("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("filter", "only-completed")
        ).andReturn();

        TodosDto todosDto = objectMapper.readValue(response.getResponse().getContentAsString(), TodosDto.class);

        // When
        assertEquals(HttpStatus.SC_OK, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(todosDto.getTodos().stream().allMatch(TodoDto::isCompleted));
    }

    @Test
    @DisplayName("Get all user todos test with only-not-completed filter")
    void getAllUserTodos_withOnlyNotCompletedFilter() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();

        var response = mockMvc.perform(
                get("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("filter", "only-not-completed")
        ).andReturn();

        TodosDto todosDto = objectMapper.readValue(response.getResponse().getContentAsString(), TodosDto.class);

        // When
        assertEquals(HttpStatus.SC_OK, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(todosDto.getTodos().stream().noneMatch(TodoDto::isCompleted));
    }

    @Test
    @DisplayName("Get all user todos test with incorrect filter")
    void getAllUserTodos_withIncorrectFilter() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();

        var response = mockMvc.perform(
                get("/todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("filter", "incorrect-filter")
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }

    @Test
    @DisplayName("Get all user todos by category test")
    void getAllUserTodosByCategory() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String category = "Home";

        var response = mockMvc.perform(
                get("/todos/" + category)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        ).andReturn();

        TodosDto todosDto = objectMapper.readValue(response.getResponse().getContentAsString(), TodosDto.class);

        // When
        assertEquals(HttpStatus.SC_OK, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(todosDto.getTodos().stream().allMatch(todoDto -> todoDto.getCategory().equals(category)));
    }

    @Test
    @DisplayName("Search todos by query test")
    void searchTodos() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String query = "text";

        var response = mockMvc.perform(
                get("/todos/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("s", query)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_OK, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
    }
}