package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;

/**
 * 접근 범위 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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
    private String name;

}
