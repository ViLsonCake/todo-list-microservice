package project.vilsoncake.todoservice.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import project.vilsoncake.todoservice.dto.TodoDto;
import project.vilsoncake.todoservice.dto.TodoRequest;
import project.vilsoncake.todoservice.dto.UserEventDto;

import java.util.List;

public interface TodoService {
    TodoDto addTodo(Jwt jwt, TodoRequest todoRequest);
    TodoDto changeCompleted(Jwt jwt, String title);
    String removeTodo(Jwt jwt, String title);
    List<TodoDto> searchTodos(Jwt jwt, String searchString, PageRequest pageRequest);
    List<TodoDto> getAllUserTodosByFilter(Jwt jwt, String filter, PageRequest pageRequest);
    List<TodoDto> getAllUserTodosByCategory(Jwt jwt, String category, PageRequest pageRequest);
    boolean changeUserUsernameInTodos(UserEventDto userEventDto);
    boolean removeAllUserTodos(UserEventDto userEventDto);
}
