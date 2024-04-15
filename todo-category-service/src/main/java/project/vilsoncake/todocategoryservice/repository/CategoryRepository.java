package project.vilsoncake.todocategoryservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import project.vilsoncake.todocategoryservice.document.CategoryDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends MongoRepository<CategoryDocument, UUID> {
    Optional<CategoryDocument> findByOwnerIgnoreCaseAndNameIgnoreCase(String owner, String name);
    List<CategoryDocument> findAllByOwnerIgnoreCase(String owner);
}
