package project.vilsoncake.todoservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import project.vilsoncake.todoservice.document.TodoDocument;
import project.vilsoncake.todoservice.dto.TodoDto;
import project.vilsoncake.todoservice.dto.TodoRequest;
import project.vilsoncake.todoservice.exception.IncorrectTodoFilterException;
import project.vilsoncake.todoservice.exception.TodoNotFoundException;
import project.vilsoncake.todoservice.repository.TodoRepository;
import project.vilsoncake.todoservice.service.TodoService;
import project.vilsoncake.todoservice.utils.TodoUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static project.vilsoncake.todoservice.constant.MessageConst.*;
import static project.vilsoncake.todoservice.constant.PatternConst.TITLE_LIKE_PATTERN_POSTFIX;
import static project.vilsoncake.todoservice.constant.PatternConst.TITLE_POSTFIX;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final TodoRepository todoRepository;
    private final TodoUtils todoUtils;

    @Override
    public TodoDto addTodo(Jwt jwt, TodoRequest todoRequest) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        TodoDto todoDto = TodoDto.fromRequest(todoRequest);

        if (todoRepository.findByTitleIgnoreCaseAndOwnerIgnoreCase(todoDto.getTitle(), username).isPresent()) {
            Query query = NativeQuery.builder()
                    .withQuery(q -> q
                            .matchPhrase(r -> r
                                    .field("title")
                                    .query(todoDto.getTitle() + TITLE_LIKE_PATTERN_POSTFIX)
                            )
                    )
                    .build();

            SearchHits<TodoDocument> searchHits = elasticsearchOperations.search(query, TodoDocument.class);

            List<TodoDocument> todosWithSameTitle = searchHits.stream().map(SearchHit::getContent).toList();

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
    public List<TodoDto> getAllUserTodosByFilter(Jwt jwt, String filter) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        return switch (filter) {
            case FILTER_ALL_TODOS ->
                    todoRepository
                            .findAllByOwnerIgnoreCase(username)
                            .stream().map(TodoDto::fromDocument).toList();
            case FILTER_ONLY_COMPLETED_TODOS ->
                    todoRepository
                            .findAllByOwnerIgnoreCaseAndCompletedIsTrue(username)
                            .stream().map(TodoDto::fromDocument).toList();
            case FILTER_ONLY_NOT_COMPLETED_TODOS ->
                    todoRepository
                            .findAllByOwnerIgnoreCaseAndCompletedIsFalse(username)
                            .stream().map(TodoDto::fromDocument).toList();
            default -> throw new IncorrectTodoFilterException(String.format("\"%s\" is incorrect filter", filter));
        };

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
