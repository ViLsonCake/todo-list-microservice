package project.vilsoncake.authorizationserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.oauth2.jwt.Jwt;
import project.vilsoncake.authorizationserver.dto.ChangeUsernameDto;
import project.vilsoncake.authorizationserver.dto.RegistrationDto;

import java.util.Map;

public interface UserService {
    Map<String, String> createUser(RegistrationDto registrationDto);
    UserRepresentation getUserByUsername(String username);
    Map<String, String> changeUsername(Jwt jwt, ChangeUsernameDto changeUsernameDto) throws JsonProcessingException;
    Map<String, String> removeUser(Jwt jwt) throws JsonProcessingException;
}
