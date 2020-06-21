package me.nuguri.common.entity;

import lombok.*;
import me.nuguri.common.enums.Scopes;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Scope extends BaseEntity {

    /**
     * 식별키
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 접근 범위
     */
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private String scope;

}
