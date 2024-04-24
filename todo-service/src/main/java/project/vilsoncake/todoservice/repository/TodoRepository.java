package project.vilsoncake.todoservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import project.vilsoncake.todoservice.document.TodoDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository extends ElasticsearchRepository<TodoDocument, UUID> {
    Optional<TodoDocument> findByTitleIgnoreCaseAndOwnerIgnoreCase(String title, String owner);
    Page<TodoDocument> findAllByOwnerIgnoreCase(String owner, Pageable pageable);
    List<TodoDocument> findAllByOwnerIgnoreCase(String owner);
    Page<TodoDocument> findAllByOwnerIgnoreCaseAndCompletedIsTrue(String owner, Pageable pageable);
    Page<TodoDocument> findAllByOwnerIgnoreCaseAndCompletedIsFalse(String owner, Pageable pageable);
    Page<TodoDocument> findAllByOwnerIgnoreCaseAndCategoryIgnoreCase(String owner, String category, Pageable pageable);
}
