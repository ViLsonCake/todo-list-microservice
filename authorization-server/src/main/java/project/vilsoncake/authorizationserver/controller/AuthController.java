package project.vilsoncake.authorizationserver.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vilsoncake.authorizationserver.dto.LoginDto;
import project.vilsoncake.authorizationserver.dto.TokenDto;
import project.vilsoncake.authorizationserver.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto> loginUser(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        return ResponseEntity.ok(authService.loginUser(loginDto, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refreshToken(@CookieValue(name = "refresh_token") String refreshToken, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken, response));
    }
}
