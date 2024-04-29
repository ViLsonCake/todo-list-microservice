package project.vilsoncake.authorizationserver.service;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.auth.InvalidCredentialsException;
import project.vilsoncake.authorizationserver.dto.LoginDto;
import project.vilsoncake.authorizationserver.dto.TokenDto;

public interface AuthService {
    TokenDto loginUser(LoginDto loginDto, HttpServletResponse response) throws InvalidCredentialsException;
    TokenDto refreshToken(String refreshToken, HttpServletResponse response);
}
