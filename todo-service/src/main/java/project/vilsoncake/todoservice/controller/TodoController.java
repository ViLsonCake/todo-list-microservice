package project.vilsoncake.todoservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import project.vilsoncake.todoservice.dto.TodoDto;
import project.vilsoncake.todoservice.dto.TodoRequest;
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
            @Valid @RequestBody TodoRequest todoRequest
    ) {
        return new ResponseEntity<>(
                Map.of("message", String
                        .format("Todo \"%s\" has been added",
                                todoService.addTodo(jwt, todoRequest)
                                        .getTitle()
                        )
                ),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Map<String, List<TodoDto>>> getAllUserTodos(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "filter", required = false, defaultValue = "all") String filter,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(Map.of("todos", todoService.getAllUserTodosByFilter(jwt, filter, PageRequest.of(page, size))));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, List<TodoDto>>> searchTodos(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "s") String searchString,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(Map.of("todos", todoService.searchTodos(jwt, searchString, PageRequest.of(page, size))));
    }

    @GetMapping("/{category}")
    public ResponseEntity<Map<String, List<TodoDto>>> getAllUserTodosByCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(Map.of("todos", todoService.getAllUserTodosByCategory(jwt, category, PageRequest.of(page, size))));
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
