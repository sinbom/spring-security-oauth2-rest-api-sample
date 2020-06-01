package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
@EqualsAndHashCode(of ="id", callSuper = false)
public class Account extends BaseEntity implements Serializable {

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

    /** 성별 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    /** 주소 */
    @Embedded
    @Column(nullable = false)
    private Address address;

    /** 권한 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** 등록 클라이언트 */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Client> clients = new ArrayList<>();

    /** 주문 목록 */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

}
