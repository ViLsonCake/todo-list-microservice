package project.vilsoncake.authorizationserver.dto;

import lombok.Data;

@Data
public class RegistrationDto {
    private String username;
    private String email;
    private String password;
}
