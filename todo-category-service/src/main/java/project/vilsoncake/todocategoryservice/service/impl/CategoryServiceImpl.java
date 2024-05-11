package project.vilsoncake.todocategoryservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import project.vilsoncake.todocategoryservice.constant.CategoryConst;
import project.vilsoncake.todocategoryservice.document.CategoryDocument;
import project.vilsoncake.todocategoryservice.dto.CategoryDto;
import project.vilsoncake.todocategoryservice.dto.UserEventDto;
import project.vilsoncake.todocategoryservice.exception.CategoryAlreadyExistsException;
import project.vilsoncake.todocategoryservice.exception.CategoryNotFoundException;
import project.vilsoncake.todocategoryservice.repository.CategoryRepository;
import project.vilsoncake.todocategoryservice.service.CategoryService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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
        if (categoryRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(username, categoryDto.getName()).isPresent() ||
                CategoryConst.DEFAULT_CATEGORIES.stream().anyMatch(category -> category.equalsIgnoreCase(categoryDto.getName()))) {
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
    public List<String> getAllCategoriesByOwner(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        List<String> userCategories = categoryRepository
                .findAllByOwnerIgnoreCase(username)
                .stream().map(CategoryDocument::getName)
                .toList();

        return Stream.concat(userCategories.stream(), CategoryConst.DEFAULT_CATEGORIES.stream()).toList();
    }

    @Override
    public boolean changeCategoriesOwnerUsername(UserEventDto userEventDto) {
        String newUsername = userEventDto.getPayload().get("newUsername");

        List<CategoryDocument> categories = categoryRepository.findAllByOwnerIgnoreCase(userEventDto.getUsername());
        categories.forEach(category -> category.setOwner(newUsername));
        categoryRepository.saveAll(categories);

        return true;
    }

    @Override
    public boolean removeAllCategoriesByOwner(UserEventDto userEventDto) {
        categoryRepository.deleteAll(
                categoryRepository.findAllByOwnerIgnoreCase(userEventDto.getUsername())
        );
        return true;
    }
}
