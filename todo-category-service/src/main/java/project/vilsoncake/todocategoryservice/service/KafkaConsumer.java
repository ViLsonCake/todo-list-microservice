package project.vilsoncake.todocategoryservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface KafkaConsumer {
    void handleUserEvent(String serializedUserEvent) throws JsonProcessingException;
}
