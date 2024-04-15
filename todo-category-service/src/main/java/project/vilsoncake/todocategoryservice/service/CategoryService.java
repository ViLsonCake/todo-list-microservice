package project.vilsoncake.todocategoryservice.service;

import org.springframework.security.oauth2.jwt.Jwt;
import project.vilsoncake.todocategoryservice.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(Jwt jwt, CategoryDto categoryDto);
    String  removeCategory(Jwt jwt, String name);
    List<String> getAllCategoriesByOwner(Jwt jwt);
    String removeAllCategoriesByOwner(Jwt jwt);
}
