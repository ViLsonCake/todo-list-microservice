package project.vilsoncake.authorizationserver.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import project.vilsoncake.authorizationserver.dto.UserEventDto;
import project.vilsoncake.authorizationserver.property.KafkaProperties;
import project.vilsoncake.authorizationserver.service.KafkaProducer;

@Service
@RequiredArgsConstructor
public class KafkaProducerImpl implements KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean sendUserEvent(UserEventDto userEventDto) throws JsonProcessingException {
        String serializedUserEvent = objectMapper.writeValueAsString(userEventDto);
        kafkaTemplate.send(kafkaProperties.getTopicName(), serializedUserEvent);

        return true;
    }
}
