package project.vilsoncake.todocategoryservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.vilsoncake.todocategoryservice.exception.CategoryAlreadyExistsException;
import project.vilsoncake.todocategoryservice.exception.CategoryNotFoundException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> categoryAlreadyExists(CategoryAlreadyExistsException exception) {
        return new ResponseEntity<>(
                Map.of("message", exception.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> categoryNotFound(CategoryNotFoundException exception) {
        return new ResponseEntity<>(
                Map.of("message", exception.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}
