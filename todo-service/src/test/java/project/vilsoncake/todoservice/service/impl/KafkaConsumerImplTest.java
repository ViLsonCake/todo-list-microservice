package project.vilsoncake.todoservice.service.impl;

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
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;
import project.vilsoncake.todoservice.document.TodoDocument;
import project.vilsoncake.todoservice.repository.TodoRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class KafkaConsumerImplTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TodoRepository todoRepository;

    @Value("${kafka.topic-name}")
    private String topicName;

    static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.11.1")
            .withStartupAttempts(10)
            .withEnv("discovery.type", "single-node");

    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withStartupAttempts(10)
            .withEmbeddedZookeeper();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("elastic.host", () -> elasticsearchContainer.getHttpHostAddress().split(":")[0]);
        registry.add("elastic.port", () -> elasticsearchContainer.getHttpHostAddress().split(":")[1]);
        registry.add("kafka.server-url", kafkaContainer::getBootstrapServers);
    }

    @BeforeAll
    static void startContainers() {
        elasticsearchContainer.start();
        kafkaContainer.start();
    }

    @AfterAll
    static void stopContainers() {
        elasticsearchContainer.stop();
        kafkaContainer.stop();
    }

    @BeforeEach
    void addInitTodos() {
        String owner = "testuser";

        TodoDocument todo1 = new TodoDocument();
        todo1.setId(UUID.randomUUID());
        todo1.setTitle("Init todo");
        todo1.setCategory("Home");
        todo1.setText("Init todo text");
        todo1.setCreatedAt(new Date());
        todo1.setCompleted(false);
        todo1.setOwner(owner);

        TodoDocument todo2 = new TodoDocument();
        todo2.setId(UUID.randomUUID());
        todo2.setTitle("Second init todo");
        todo2.setCategory("Home");
        todo2.setText("Second init todo text");
        todo2.setCreatedAt(new Date());
        todo2.setCompleted(true);
        todo2.setOwner(owner);

        TodoDocument todo3 = new TodoDocument();
        todo3.setId(UUID.randomUUID());
        todo3.setTitle("Third init todo");
        todo3.setCategory("Home");
        todo3.setText("Third init todo text");
        todo3.setCreatedAt(new Date());
        todo3.setCompleted(true);
        todo3.setOwner("testuser");

        TodoDocument todo4 = new TodoDocument();
        todo4.setId(UUID.randomUUID());
        todo4.setTitle("Fourth init todo");
        todo4.setCategory("Work");
        todo4.setText("Fourth init todo text");
        todo4.setCreatedAt(new Date());
        todo4.setCompleted(true);
        todo4.setOwner(owner);

        todoRepository.saveAll(List.of(todo1, todo2, todo3, todo4));
    }

    @AfterEach
    void removeInitTodos() {
        todoRepository.deleteAll();
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
        assertTrue(todoRepository.findAllByOwnerIgnoreCase(username).isEmpty());
        assertFalse(todoRepository.findAllByOwnerIgnoreCase(newUsername).isEmpty());
    }

    @Test
    @DisplayName("Consume kafka message for remove user test")
    void handleUserEvent_removeUserMessage() throws Exception {
        String username = "testuser";
        String serializedMessage = String.format("{\"type\":\"user-remove\",\"username\":\"%s\",\"payload\":{}}", username);

        List<TodoDocument> todosBeforeSend = todoRepository.findAllByOwnerIgnoreCase(username);

        kafkaTemplate.send(topicName, serializedMessage);

        Thread.sleep(1500);

        List<TodoDocument> todosAfterSend = todoRepository.findAllByOwnerIgnoreCase(username);

        // When
        assertFalse(todosBeforeSend.isEmpty());
        assertTrue(todosAfterSend.isEmpty());
    }
}