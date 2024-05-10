package project.vilsoncake.todoservice.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "elastic")
public class ElasticsearchProperties {
    private String host;
    private int port;
}
