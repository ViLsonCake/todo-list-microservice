package project.vilsoncake.todoservice.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${kafka.server-url}")
    private String serverUrl;

    @Value("${kafka.topic-name}")
    private String topicName;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> config = new HashMap<>();
        config.put(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                serverUrl
        );

        return new KafkaAdmin(config);
    }

    @Bean
    public NewTopic newTopic() {
        return new NewTopic(
                topicName,
                5,
                (short) 1
        );
    }
}
