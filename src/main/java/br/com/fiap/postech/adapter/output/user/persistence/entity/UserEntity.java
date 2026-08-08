package br.com.fiap.postech.adapter.output.user.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Arrays;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "role", columnDefinition = "text[]")
    private String[] roles;


    public List<String> getRolesList() {
        return roles == null ? List.of() : Arrays.asList(roles);
    }

    public void setRolesList(List<String> roles) {
        this.roles = roles == null ? null : roles.toArray(new String[0]);
    }
    
}