package me.nuguri.auth.entity;

import lombok.*;
import me.nuguri.common.enums.Role;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 계정 엔티티
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of ="id")
@Builder
public class Account implements Serializable {

    /** PK */
    @Id
    @GeneratedValue
    private Long id;

    /** 이메일 */
    @Column(unique = true, nullable = false, updatable = false)
    private String email;

    /** 비밀번호 */
    @Column(nullable = false)
    private String password;

    /** 권한 */
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    /** 등록 클라이언트 */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Client> clients;

}
