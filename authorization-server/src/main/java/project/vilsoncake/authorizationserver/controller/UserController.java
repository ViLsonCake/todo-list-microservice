package project.vilsoncake.authorizationserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import project.vilsoncake.authorizationserver.dto.ChangeUsernameDto;
import project.vilsoncake.authorizationserver.dto.RegistrationDto;
import project.vilsoncake.authorizationserver.service.UserService;

import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@RequestBody RegistrationDto registrationDto) {
        return new ResponseEntity<>(
                userService.createUser(registrationDto),
                CREATED
        );
    }

    @PatchMapping
    public ResponseEntity<Map<String, String>> changeUsername(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ChangeUsernameDto changeUsernameDto
    ) {
        return ResponseEntity.ok(userService.changeUsername(jwt, changeUsernameDto));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.removeUser(jwt));
    }
}
