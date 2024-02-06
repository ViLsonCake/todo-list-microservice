package project.vilsoncake.authorizationserver.service;

import project.vilsoncake.authorizationserver.dto.RegistrationDto;

import java.util.Map;

public interface UserService {
    Map<String, String> createUser(RegistrationDto registrationDto);
}
