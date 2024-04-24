package project.vilsoncake.todoservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import project.vilsoncake.todoservice.dto.UserEventDto;
import project.vilsoncake.todoservice.property.UserEventProperties;
import project.vilsoncake.todoservice.service.KafkaConsumer;
import project.vilsoncake.todoservice.service.TodoService;

@Service
@RequiredArgsConstructor
public class KafkaConsumerImpl implements KafkaConsumer {

    private final TodoService todoService;
    private final ObjectMapper objectMapper;
    private final UserEventProperties userEventProperties;

    @KafkaListener(topics = "${kafka.topic-name}", groupId = "${kafka.group-id}")
    @Override
    public void handleUserEvent(String serializedUserEvent) throws JsonProcessingException {
        UserEventDto userEventDto = objectMapper.readValue(serializedUserEvent, UserEventDto.class);

        if (userEventDto.getType().equals(userEventProperties.getUsernameChangeEventType())) {
            todoService.changeUserUsernameInTodos(userEventDto);
        } else if (userEventDto.getType().equals(userEventProperties.getUserRemoveEventType())) {
            todoService.removeAllUserTodos(userEventDto);
        }
    }
}
