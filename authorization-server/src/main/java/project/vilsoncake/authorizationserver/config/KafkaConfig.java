package project.vilsoncake.authorizationserver.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import project.vilsoncake.authorizationserver.property.KafkaProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> config = new HashMap<>();
        config.put(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getUrl()
        );

        return new KafkaAdmin(config);
    }

    @Bean
    public NewTopic newTopic() {
        return new NewTopic(
                kafkaProperties.getTopicName(),
                kafkaProperties.getPartitionsCount(),
                kafkaProperties.getReplicationFactor()
        );
    }
}
