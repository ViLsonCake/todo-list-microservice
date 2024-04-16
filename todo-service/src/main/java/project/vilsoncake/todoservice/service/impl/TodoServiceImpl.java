package project.vilsoncake.todoservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import project.vilsoncake.todoservice.document.TodoDocument;
import project.vilsoncake.todoservice.dto.TodoDto;
import project.vilsoncake.todoservice.exception.TodoNotFoundException;
import project.vilsoncake.todoservice.repository.TodoRepository;
import project.vilsoncake.todoservice.service.TodoService;
import project.vilsoncake.todoservice.utils.TodoUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static project.vilsoncake.todoservice.constant.PatternConst.TITLE_LIKE_PATTERN_POSTFIX;
import static project.vilsoncake.todoservice.constant.PatternConst.TITLE_POSTFIX;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoUtils todoUtils;

    @Override
    public TodoDto addTodo(Jwt jwt, TodoDto todoDto) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        if (todoRepository.findByTitleIgnoreCaseAndOwnerIgnoreCase(todoDto.getTitle(), username).isPresent()) {
            List<TodoDocument> todosWithSameTitle = todoRepository
                    .findAllByTitleLikeAndOwnerIgnoreCase(
                            todoDto.getTitle() + TITLE_LIKE_PATTERN_POSTFIX,
                            username
                    );

            if (todosWithSameTitle.size() == 1) {
                todoDto.setTitle(todoDto.getTitle() + String.format(TITLE_POSTFIX, 1));
            } else if (todosWithSameTitle.size() > 1) {
                int numberOfDuplicates = todoUtils.getDuplicateCount(todosWithSameTitle);
                todoDto.setTitle(todoDto.getTitle() + String.format(TITLE_POSTFIX, numberOfDuplicates + 1));
            }
        }

        TodoDocument todo = new TodoDocument();
        todo.setId(UUID.randomUUID());
        todo.setTitle(todoDto.getTitle());
        todo.setCategory(todoDto.getCategory());
        todo.setText(todoDto.getText());
        todo.setCompleted(todoDto.isCompleted());
        todo.setCreatedAt(new Date());
        todo.setOwner(username);

        todoRepository.save(todo);

        return todoDto;
    }

    @Override
    public TodoDto changeCompleted(Jwt jwt, String title) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        TodoDocument todo = todoRepository.findByTitleIgnoreCaseAndOwnerIgnoreCase(title, username)
                .orElseThrow(() -> new TodoNotFoundException(String.format("Todo \"%s\" not found", title)));
        todo.setCompleted(!todo.isCompleted());
        todoRepository.save(todo);

        return TodoDto.fromDocument(todo);
    }

    @Override
    public String removeTodo(Jwt jwt, String title) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        TodoDocument todo = todoRepository.findByTitleIgnoreCaseAndOwnerIgnoreCase(title, username)
                .orElseThrow(() -> new TodoNotFoundException(String.format("Todo \"%s\" not found", title)));
        todoRepository.delete(todo);
        return title;
    }

    @Override
    public List<TodoDto> getAllUserTodos(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        return todoRepository.findAllByOwnerIgnoreCase(username).stream().map(TodoDto::fromDocument).toList();
    }

    @Override
    public List<TodoDto> getAllUserTodosByCategory(Jwt jwt, String category) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        return todoRepository.findAllByOwnerIgnoreCaseAndCategoryIgnoreCase(username, category).stream().map(TodoDto::fromDocument).toList();
    }
}
