package project.vilsoncake.authorizationserver.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vilsoncake.authorizationserver.dto.LoginDto;
import project.vilsoncake.authorizationserver.dto.RegistrationDto;
import project.vilsoncake.authorizationserver.dto.TokenDto;
import project.vilsoncake.authorizationserver.service.AuthService;
import project.vilsoncake.authorizationserver.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@RequestBody RegistrationDto registrationDto) {
        return ResponseEntity.ok(userService.createUser(registrationDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> loginUser(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        return ResponseEntity.ok(authService.loginUser(loginDto, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refreshToken(@CookieValue(name = "refresh_token") String refreshToken, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken, response));
    }
}
