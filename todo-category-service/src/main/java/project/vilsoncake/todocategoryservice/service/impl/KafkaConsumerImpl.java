package project.vilsoncake.todocategoryservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import project.vilsoncake.todocategoryservice.dto.UserEventDto;
import project.vilsoncake.todocategoryservice.property.UserEventProperties;
import project.vilsoncake.todocategoryservice.service.CategoryService;
import project.vilsoncake.todocategoryservice.service.KafkaConsumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerImpl implements KafkaConsumer {

    private final CategoryService categoryService;
    private final UserEventProperties userEventProperties;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic-name}", groupId = "${kafka.group-id}")
    @Override
    public void handleUserEvent(String serializedUserEvent) throws JsonProcessingException {
        UserEventDto userEventDto = objectMapper.readValue(serializedUserEvent, UserEventDto.class);

        if (userEventDto.getType().equals(userEventProperties.getUsernameChangeEventType())) {
            categoryService.changeCategoriesOwnerUsername(userEventDto);
        } else if (userEventDto.getType().equals(userEventProperties.getUserRemoveEventType())) {
            categoryService.removeAllCategoriesByOwner(userEventDto);
        }
    }
}
