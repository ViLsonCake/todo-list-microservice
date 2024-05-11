package project.vilsoncake.todocategoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import project.vilsoncake.todocategoryservice.dto.CategoryDto;
import project.vilsoncake.todocategoryservice.service.CategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Map<String, String>> addCategory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CategoryDto categoryDto
    ) {
        return new ResponseEntity<>(
                Map.of(
                        "message", String.format("Category %s has been added", categoryService.addCategory(jwt, categoryDto).getName())
                ), HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getAllCategoriesByOwner(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                Map.of("categories", categoryService.getAllCategoriesByOwner(jwt))
        );
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeCategory(@AuthenticationPrincipal Jwt jwt, @RequestParam String name) {
        return ResponseEntity.ok(
                Map.of("message", String.format("Category %s has been removed", categoryService.removeCategory(jwt, name)))
        );
    }
}
