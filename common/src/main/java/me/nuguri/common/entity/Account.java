package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.enums.Role;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 계정 엔티티
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of ="id")
public class Account implements Serializable {

    private static final Long serialVersionUID = 1L;

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

    @Column(nullable = false)
    private String name;

    /** 권한 */
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    /** 등록 클라이언트 */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Client> clients = new ArrayList<>();

}