package project.vilsoncake.todocategoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vilsoncake.todocategoryservice.document.CategoryDocument;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private String name;

    public static CategoryDto fromDocument(CategoryDocument categoryDocument) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(categoryDocument.getName());
        return categoryDto;
    }
}
