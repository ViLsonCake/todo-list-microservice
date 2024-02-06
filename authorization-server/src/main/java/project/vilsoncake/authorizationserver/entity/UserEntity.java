package project.vilsoncake.authorizationserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_")
@Data
@NoArgsConstructor
public class UserEntity {

    @Id
    private String id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;
}
