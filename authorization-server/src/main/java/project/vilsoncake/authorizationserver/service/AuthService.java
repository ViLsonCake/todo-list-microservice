package project.vilsoncake.authorizationserver.service;

import jakarta.servlet.http.HttpServletResponse;
import project.vilsoncake.authorizationserver.dto.LoginDto;
import project.vilsoncake.authorizationserver.dto.TokenDto;

public interface AuthService {
    TokenDto loginUser(LoginDto loginDto, HttpServletResponse response);
    TokenDto refreshToken(String refreshToken, HttpServletResponse response);
}
