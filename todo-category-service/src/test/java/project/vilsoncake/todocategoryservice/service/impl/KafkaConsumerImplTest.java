package project.vilsoncake.todocategoryservice.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import project.vilsoncake.todocategoryservice.document.CategoryDocument;
import project.vilsoncake.todocategoryservice.repository.CategoryRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class KafkaConsumerImplTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${kafka.topic-name}")
    private String topicName;

    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withStartupAttempts(10)
            .withEmbeddedZookeeper();

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest")
            .withStartupAttempts(10);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.server-url", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    static void startContainers() {
        kafkaContainer.start();
        mongoDBContainer.start();
    }

    @AfterAll
    static void stopContainers() {
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
    @DisplayName("Consume kafka message for change user username test")
    void handleUserEvent_changeUsernameMessage() throws Exception {
        String username = "testuser";
        String newUsername = "newUsername";
        String serializedMessage = String.format("{\"type\":\"change-username\",\"username\":\"%s\",\"payload\":{\"newUsername\":\"%s\"}}", username, newUsername);

        kafkaTemplate.send(topicName, serializedMessage);

        Thread.sleep(1500);

        // When
        assertTrue(categoryRepository.findAllByOwnerIgnoreCase(username).isEmpty());
        assertFalse(categoryRepository.findAllByOwnerIgnoreCase(newUsername).isEmpty());
    }

    @Test
    @DisplayName("Consume kafka message for remover user test")
    void handleUserEvent_removeUserMessage() throws Exception {
        String username = "testuser";
        String serializedMessage = String.format("{\"type\":\"user-remove\",\"username\":\"%s\",\"payload\":{}}", username);

        List<CategoryDocument> categoriesBeforeSend = categoryRepository.findAllByOwnerIgnoreCase(username);

        kafkaTemplate.send(topicName, serializedMessage);

        Thread.sleep(1500);

        List<CategoryDocument> categoriesAfterSend = categoryRepository.findAllByOwnerIgnoreCase(username);

        // When
        assertFalse(categoriesBeforeSend.isEmpty());
        assertTrue(categoriesAfterSend.isEmpty());
    }
}