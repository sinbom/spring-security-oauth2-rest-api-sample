package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Resource extends BaseEntity {

    /**
     * 식별키
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 리소스 이름
     */
    @Column(nullable = false, unique = true)
    private String resourceId;

}
