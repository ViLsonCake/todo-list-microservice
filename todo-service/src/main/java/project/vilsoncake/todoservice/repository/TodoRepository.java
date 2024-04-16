package project.vilsoncake.todoservice.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import project.vilsoncake.todoservice.document.TodoDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository extends ElasticsearchRepository<TodoDocument, UUID> {
    Optional<TodoDocument> findByTitleIgnoreCaseAndOwnerIgnoreCase(String title, String owner);
    List<TodoDocument> findAllByTitleLikeAndOwnerIgnoreCase(String likePattern, String owner);
    List<TodoDocument> findAllByOwnerIgnoreCase(String owner);
    List<TodoDocument> findAllByOwnerIgnoreCaseAndCompletedIsTrue(String owner);
    List<TodoDocument> findAllByOwnerIgnoreCaseAndCategoryIgnoreCase(String owner, String category);
}
