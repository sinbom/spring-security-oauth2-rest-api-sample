package me.nuguri.client.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.nuguri.client.enums.LoginType;
import me.nuguri.common.enums.Gender;
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

    /** 이름, 닉네임 */
    @Column(nullable = false)
    private String name;

    /** 로그인 타입 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private LoginType loginType;

    /** 권한 */
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;




}
