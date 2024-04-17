package project.vilsoncake.todoservice.service;

import org.springframework.security.oauth2.jwt.Jwt;
import project.vilsoncake.todoservice.dto.TodoDto;
import project.vilsoncake.todoservice.dto.TodoRequest;

import java.util.List;

public interface TodoService {
    TodoDto addTodo(Jwt jwt, TodoRequest todoRequest);
    TodoDto changeCompleted(Jwt jwt, String title);
    String removeTodo(Jwt jwt, String title);
    List<TodoDto> getAllUserTodosByFilter(Jwt jwt, String filter);
    List<TodoDto> getAllUserTodosByCategory(Jwt jwt, String category);
}
