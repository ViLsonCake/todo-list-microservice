package project.vilsoncake.authorizationserver.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import project.vilsoncake.authorizationserver.dto.RegistrationDto;
import project.vilsoncake.authorizationserver.entity.UserEntity;
import project.vilsoncake.authorizationserver.property.KeycloakProperties;
import project.vilsoncake.authorizationserver.repository.UserRepository;
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
    private final UserRepository userRepository;

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

        if (response.getStatus() != 201) {
            throw new RuntimeException("User has not been added");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setId(uuid);
        userEntity.setUsername(registrationDto.getUsername());
        userEntity.setEmail(registrationDto.getEmail());
        userRepository.save(userEntity);

        return Map.of("message", String.format("User \"%s\" added", registrationDto.getUsername()));
    }
}
