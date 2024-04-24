package project.vilsoncake.todocategoryservice.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "user")
public class UserEventProperties {
    private String usernameChangeEventType;
    private String userRemoveEventType;
}