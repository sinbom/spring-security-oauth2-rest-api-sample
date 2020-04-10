package me.nuguri.auth.entity;

import lombok.*;
import me.nuguri.auth.common.Role;

import javax.persistence.*;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of ="id")
@Builder
public class Account {

    /** PK */
    @Id
    @GeneratedValue
    private Long id;

    /** 이메일 */
    @Column(unique = true)
    private String email;

    /** 비밀번호 */
    private String password;

    /** 권한 */
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

}
