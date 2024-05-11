package project.vilsoncake.todoservice.dto;

import java.util.List;

public class TodosDto {
    private List<TodoDto> todos;

    public List<TodoDto> getTodos() {
        return todos;
    }

    public void setTodos(List<TodoDto> todos) {
        this.todos = todos;
    }
}
