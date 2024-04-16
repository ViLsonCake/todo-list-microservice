package project.vilsoncake.todoservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import project.vilsoncake.todoservice.dto.TodoDto;
import project.vilsoncake.todoservice.service.TodoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<Map<String, String>> addTodo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody TodoDto todoDto
    ) {
        return new ResponseEntity<>(
                Map.of("message", String.format("Todo \"%s\" has been added", todoService.addTodo(jwt, todoDto).getTitle())),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Map<String, List<TodoDto>>> getAllUserTodos(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of("todos", todoService.getAllUserTodos(jwt)));
    }

    @GetMapping("/{category}")
    public ResponseEntity<Map<String, List<TodoDto>>> getAllUserTodosByCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String category
    ) {
        return ResponseEntity.ok(Map.of("todos", todoService.getAllUserTodosByCategory(jwt, category)));
    }

    @PatchMapping
    public ResponseEntity<Map<String, TodoDto>> changeCompleted(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String title
    ) {
        return ResponseEntity.ok(Map.of("todo", todoService.changeCompleted(jwt, title)));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeTodo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String title
    ) {
        return ResponseEntity.ok(
                Map.of(
                        "message", String
                                .format(
                                        "Todo \"%s\" has been removed", todoService.removeTodo(jwt, title)
                                )
                )
        );
    }
}
