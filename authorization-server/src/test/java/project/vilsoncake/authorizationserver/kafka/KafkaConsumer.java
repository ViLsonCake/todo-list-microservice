package project.vilsoncake.authorizationserver.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private boolean messageConsumed = false;
    private String payload;

    @KafkaListener(topics = "${kafka.topic-name}", groupId = "${kafka.group-id}")
    public void consumeMessage(String serializedMessage) {
        System.out.println(serializedMessage);
        messageConsumed = true;
        payload = serializedMessage;
    }

    public boolean isMessageConsumed() {
        return messageConsumed;
    }

    public void reset() {
        this.messageConsumed = false;
        this.payload = null;
    }

    public String getPayload() {
        return payload;
    }
}
