package project.vilsoncake.todocategoryservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import project.vilsoncake.todocategoryservice.document.CategoryDocument;
import project.vilsoncake.todocategoryservice.dto.CategoryDto;
import project.vilsoncake.todocategoryservice.exception.CategoryAlreadyExistsException;
import project.vilsoncake.todocategoryservice.exception.CategoryNotFoundException;
import project.vilsoncake.todocategoryservice.repository.CategoryRepository;
import project.vilsoncake.todocategoryservice.service.CategoryService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto addCategory(Jwt jwt, CategoryDto categoryDto) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }
        if (categoryRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(username, categoryDto.getName()).isPresent()) {
            throw new CategoryAlreadyExistsException(String.format("Category \"%s\" already exists", categoryDto.getName()));
        }

        CategoryDocument category = new CategoryDocument(
                UUID.randomUUID(),
                categoryDto.getName(),
                username
        );
        categoryRepository.save(category);
        return categoryDto;
    }

    @Override
    public String removeCategory(Jwt jwt, String name) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        CategoryDocument category = categoryRepository
                .findByOwnerIgnoreCaseAndNameIgnoreCase(username, name)
                .orElseThrow(() ->
                        new CategoryNotFoundException(String.format("Category %s not found", name))
                );

        categoryRepository.delete(category);
        return name;
    }

    @Override
    public List<CategoryDto> getAllCategoriesByOwner(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        return categoryRepository
                .findAllByOwnerIgnoreCase(username)
                .stream().map(CategoryDto::fromDocument)
                .toList();
    }

    @Override
    public String removeAllCategoriesByOwner(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        categoryRepository.deleteAll(categoryRepository.findAllByOwnerIgnoreCase(username));
        return username;
    }
}
