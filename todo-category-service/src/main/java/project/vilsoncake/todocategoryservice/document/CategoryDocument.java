package project.vilsoncake.todocategoryservice.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDocument {
    @Id
    private UUID id;
    private String name;
    private String owner;
}
