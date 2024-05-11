package project.vilsoncake.todocategoryservice.controller;

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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import project.vilsoncake.todocategoryservice.constant.CategoryConst;
import project.vilsoncake.todocategoryservice.document.CategoryDocument;
import project.vilsoncake.todocategoryservice.dto.CategoriesDto;
import project.vilsoncake.todocategoryservice.keycloak.KeycloakUtils;
import project.vilsoncake.todocategoryservice.repository.CategoryRepository;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeycloakUtils keycloakUtils;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:22.0.5")
            .withStartupAttempts(10)
            .withRealmImportFile("keycloak/realm-export.json");

    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withStartupAttempts(10)
            .withEmbeddedZookeeper();

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest")
            .withStartupAttempts(10);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloakContainer.getAuthServerUrl() + "/realms/todo-realm");
        registry.add("keycloak.server-url", keycloakContainer::getAuthServerUrl);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    static void startContainers() {
        keycloakContainer.start();
        kafkaContainer.start();
        mongoDBContainer.start();
    }

    @AfterAll
    static void stopContainers() {
        keycloakContainer.stop();
        kafkaContainer.stop();
        mongoDBContainer.stop();
    }

    @BeforeEach
    void addInitCategories() {
        String owner = "testuser";

        CategoryDocument category1 = new CategoryDocument();
        category1.setId(UUID.randomUUID());
        category1.setName("Init category");
        category1.setOwner(owner);

        CategoryDocument category2 = new CategoryDocument();
        category2.setId(UUID.randomUUID());
        category2.setName("Second category");
        category2.setOwner(owner);

        CategoryDocument category3 = new CategoryDocument();
        category3.setId(UUID.randomUUID());
        category3.setName("Third category");
        category3.setOwner(owner);

        categoryRepository.saveAll(List.of(category1, category2, category3));
    }

    @AfterEach
    void removeInitCategories() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("Add new category test with valid data")
    void addCategory_withValidData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String username = "testuser";
        String categoryName = "New category";
        String jsonCategoryRequest = String.format("""
                {
                  "name": "%s"
                }
                """, categoryName);

        var response = mockMvc.perform(
                post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoryRequest)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_CREATED, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertTrue(categoryRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(username, categoryName).isPresent());
    }

    @Test
    @DisplayName("Add new category test with already used name")
    void addCategory_withAlreadyUsedName() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String categoryName = "Init category";
        String jsonCategoryRequest = String.format("""
                {
                  "name": "%s"
                }
                """, categoryName);

        var response = mockMvc.perform(
                post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoryRequest)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_CONFLICT, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }

    @Test
    @DisplayName("Add new category test with default category name")
    void addCategory_withDefaultCategoryName() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String categoryName = CategoryConst.DEFAULT_CATEGORIES.get(new Random().nextInt(CategoryConst.DEFAULT_CATEGORIES.size()));
        String jsonCategoryRequest = String.format("""
                {
                  "name": "%s"
                }
                """, categoryName);

        var response = mockMvc.perform(
                post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoryRequest)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_CONFLICT, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }

    @Test
    @DisplayName("Get all user categories test")
    void getAllUserCategories() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();

        var response = mockMvc.perform(
                get("/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        ).andReturn();

        CategoriesDto categoriesDto = objectMapper.readValue(response.getResponse().getContentAsString(), CategoriesDto.class);

        // When
        assertEquals(HttpStatus.SC_OK, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(categoriesDto.getCategories().containsAll(CategoryConst.DEFAULT_CATEGORIES));
    }

    @Test
    @DisplayName("Remove category test with valid category name")
    void removeCategory_withValidData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String username = "testuser";
        String categoryName = "Init category";

        var response = mockMvc.perform(
                delete("/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("name", categoryName)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_OK, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
        assertTrue(categoryRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(username, categoryName).isEmpty());
    }

    @Test
    @DisplayName("Remove category test with already used category name")
    void removeCategory_withInvalidData() throws Exception {
        String accessToken = keycloakUtils.getTestUserAuthTokens().getAccessToken();
        String categoryName = "non-existing category";

        var response = mockMvc.perform(
                delete("/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("name", categoryName)
        ).andReturn();

        // When
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getResponse().getStatus());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, response.getResponse().getContentType());
        assertTrue(response.getResponse().getContentAsString().contains("\"message\":"));
    }
}