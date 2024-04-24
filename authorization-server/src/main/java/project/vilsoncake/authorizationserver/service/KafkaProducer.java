package project.vilsoncake.authorizationserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import project.vilsoncake.authorizationserver.dto.UserEventDto;

public interface KafkaProducer {
    boolean sendUserEvent(UserEventDto userEventDto) throws JsonProcessingException;
}
