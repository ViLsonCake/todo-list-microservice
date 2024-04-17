package project.vilsoncake.todoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vilsoncake.todoservice.document.TodoDocument;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoDto {
    private String title;
    private String category;
    private String text;
    private boolean completed;

    public static TodoDto fromDocument(TodoDocument todoDocument) {
        TodoDto todoDto = new TodoDto();
        todoDto.setTitle(todoDocument.getTitle());
        todoDto.setCategory(todoDocument.getCategory());
        todoDto.setText(todoDocument.getText());
        todoDto.setCompleted(todoDocument.isCompleted());
        return todoDto;
    }

    public static TodoDto fromRequest(TodoRequest todoRequest) {
        TodoDto todoDto = new TodoDto();
        todoDto.setTitle(todoRequest.getTitle().trim());
        todoDto.setCategory(todoRequest.getCategory().trim());
        todoDto.setText(todoRequest.getText().trim());
        todoDto.setCompleted(false);
        return todoDto;
    }
}
