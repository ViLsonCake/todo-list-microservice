package project.vilsoncake.todoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static project.vilsoncake.todoservice.constant.MessageConst.*;
import static project.vilsoncake.todoservice.constant.PatternConst.REGEX_CATEGORY_VALIDATION_PATTERN;
import static project.vilsoncake.todoservice.constant.PatternConst.REGEX_TITLE_VALIDATION_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoRequest {
    @Pattern(regexp = REGEX_TITLE_VALIDATION_PATTERN, message = TITLE_NOT_VALID_MESSAGE)
    private String title;

    @Pattern(regexp = REGEX_CATEGORY_VALIDATION_PATTERN, message = CATEGORY_NOT_VALID_MESSAGE)
    private String category;

    @NotBlank(message = TEXT_EMPTY_MESSAGE)
    private String text;
}
