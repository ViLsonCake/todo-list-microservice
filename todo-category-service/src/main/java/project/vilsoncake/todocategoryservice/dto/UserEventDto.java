package project.vilsoncake.todocategoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {
    private String type;
    private String username;
    private Map<String, String> payload;
}