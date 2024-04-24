package project.vilsoncake.authorizationserver.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    private String url;
    private String topicName;
    private int partitionsCount;
    private short replicationFactor;
}
