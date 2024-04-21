package project.vilsoncake.authorizationserver.repository;

import org.springframework.data.repository.CrudRepository;
import project.vilsoncake.authorizationserver.entity.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, String> {
    UserEntity findByUsernameIgnoreCase(String username);
}
