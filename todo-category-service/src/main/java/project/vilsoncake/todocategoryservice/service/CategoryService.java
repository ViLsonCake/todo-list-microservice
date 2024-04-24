package project.vilsoncake.todocategoryservice.service;

import org.springframework.security.oauth2.jwt.Jwt;
import project.vilsoncake.todocategoryservice.dto.CategoryDto;
import project.vilsoncake.todocategoryservice.dto.UserEventDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(Jwt jwt, CategoryDto categoryDto);
    String  removeCategory(Jwt jwt, String name);
    List<String> getAllCategoriesByOwner(Jwt jwt);
    boolean changeCategoriesOwnerUsername(UserEventDto userEventDto);
    boolean removeAllCategoriesByOwner(UserEventDto userEventDto);
}
