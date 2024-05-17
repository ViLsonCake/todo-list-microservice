package project.vilsoncake.authorizationserver.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import project.vilsoncake.authorizationserver.property.KafkaProperties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic newTopic() {
        return new NewTopic(
                kafkaProperties.getTopicName(),
                kafkaProperties.getPartitionsCount(),
                kafkaProperties.getReplicationFactor()
        );
    }
}
