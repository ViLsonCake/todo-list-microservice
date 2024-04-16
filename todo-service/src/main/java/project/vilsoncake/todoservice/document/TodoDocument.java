package project.vilsoncake.todoservice.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.UUID;

@Document(indexName = "todo")
@Data
@NoArgsConstructor
public class TodoDocument {

    @Id
    private UUID id;

    @Field(name = "title", type = FieldType.Text)
    private String title;

    @Field(name = "category", type = FieldType.Text)
    private String category;

    @Field(name = "text", type = FieldType.Text)
    private String text;

    @Field(name = "created_at", type = FieldType.Date)
    private Date createdAt;

    @Field(name = "completed", type = FieldType.Boolean)
    private boolean completed;

    @Field(name = "owner", type = FieldType.Text)
    private String owner;
}
