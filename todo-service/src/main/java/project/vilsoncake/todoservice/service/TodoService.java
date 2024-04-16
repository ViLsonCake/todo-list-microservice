package project.vilsoncake.todoservice.service;

import org.springframework.security.oauth2.jwt.Jwt;
import project.vilsoncake.todoservice.dto.TodoDto;

import java.util.List;

public interface TodoService {
    TodoDto addTodo(Jwt jwt, TodoDto todoDto);
    TodoDto changeCompleted(Jwt jwt, String title);
    String removeTodo(Jwt jwt, String title);
    List<TodoDto> getAllUserTodos(Jwt jwt);
    List<TodoDto> getAllUserTodosByCategory(Jwt jwt, String category);
}
