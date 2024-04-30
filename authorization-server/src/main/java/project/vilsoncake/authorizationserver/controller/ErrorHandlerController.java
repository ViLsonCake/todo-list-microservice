package project.vilsoncake.authorizationserver.controller;

import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.vilsoncake.authorizationserver.exception.InvalidRefreshTokenException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> invalidCredentialsException(InvalidCredentialsException exception) {
        return new ResponseEntity<>(Map.of("message", exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> invalidRefreshTokenException(InvalidRefreshTokenException exception) {
        return new ResponseEntity<>(Map.of("message", exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }
}
