package project.vilsoncake.authorizationserver.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vilsoncake.authorizationserver.dto.ChangeUsernameDto;
import project.vilsoncake.authorizationserver.dto.RegistrationDto;
import project.vilsoncake.authorizationserver.dto.UserEventDto;
import project.vilsoncake.authorizationserver.entity.UserEntity;
import project.vilsoncake.authorizationserver.exception.UserAlreadyExistsException;
import project.vilsoncake.authorizationserver.property.KeycloakProperties;
import project.vilsoncake.authorizationserver.property.UserEventProperties;
import project.vilsoncake.authorizationserver.repository.UserRepository;
import project.vilsoncake.authorizationserver.service.KafkaProducer;
import project.vilsoncake.authorizationserver.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;
    private final UserEventProperties userEventProperties;
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    @Override
    public Map<String, String> createUser(RegistrationDto registrationDto) {
        String uuid = UUID.randomUUID().toString();

        UserRepresentation user = new UserRepresentation();
        user.setId(uuid);
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setRealmRoles(List.of("user"));

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(registrationDto.getPassword());
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));

        UsersResource usersResponse = keycloak.realm(keycloakProperties.getRealm()).users();
        Response response = usersResponse.create(user);

        if (response.getStatus() == HttpStatus.SC_CONFLICT) {
            throw new UserAlreadyExistsException("User with same username or email already exists");
        }

        if (response.getStatus() != HttpStatus.SC_CREATED) {
            throw new RuntimeException("User has not been added");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setId(uuid);
        userEntity.setUsername(registrationDto.getUsername());
        userEntity.setEmail(registrationDto.getEmail());
        userRepository.save(userEntity);

        return Map.of("message", String.format("User \"%s\" added", registrationDto.getUsername()));
    }

    @Override
    public UserRepresentation getUserByUsername(String username) {
        UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
        return usersResource.searchByUsername(username, true).get(0);
    }

    @Transactional
    @Override
    public Map<String, String> changeUsername(Jwt jwt, ChangeUsernameDto changeUsernameDto) throws JsonProcessingException {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        UserRepresentation userRepresentation = getUserByUsername(username);
        userRepresentation.setUsername(changeUsernameDto.getNewUsername());

        if (userRepository.findByUsernameIgnoreCase(changeUsernameDto.getNewUsername()) != null) {
            throw new UserAlreadyExistsException(String.format("User \"%s\" already exists", changeUsernameDto.getNewUsername()));
        }

        UserEntity userEntity = userRepository.findByUsernameIgnoreCase(username);
        userEntity.setUsername(changeUsernameDto.getNewUsername());

        keycloak.realm(keycloakProperties.getRealm())
                .users()
                .get(userRepresentation.getId())
                .update(userRepresentation);

        UserEventDto changeUserEventDto = new UserEventDto(
                userEventProperties.getUsernameChangeEventType(),
                username,
                Map.of(
                        "newUsername",
                        changeUsernameDto.getNewUsername()
                )
        );

        kafkaProducer.sendUserEvent(changeUserEventDto);

        return Map.of("message", "Username has been changed");
    }

    @Override
    public Map<String, String> removeUser(Jwt jwt) throws JsonProcessingException {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        UserRepresentation userRepresentation = getUserByUsername(username);

        Response response = keycloak.realm(keycloakProperties.getRealm())
                .users()
                .delete(userRepresentation.getId());

        if (response.getStatus() < 300 && response.getStatus() >= 200) {
            UserEntity userEntity = userRepository.findByUsernameIgnoreCase(username);
            userRepository.delete(userEntity);
            UserEventDto removeUserEventDto = new UserEventDto(
                    userEventProperties.getUserRemoveEventType(),
                    username,
                    Map.of()
            );
            kafkaProducer.sendUserEvent(removeUserEventDto);

            return Map.of("message", String.format("User \"%s\" has been removed", username));
        }

        throw new UsernameNotFoundException("User not been removed");
    }
}
