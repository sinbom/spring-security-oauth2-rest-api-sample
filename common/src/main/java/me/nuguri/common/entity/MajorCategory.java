package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 상위 카테고리 엔티티
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class MajorCategory extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 하위 카테고리 */
    @OneToMany(mappedBy = "majorCategory", cascade = CascadeType.ALL)
    private List<MinorCategory> minorCategories = new ArrayList<>();

}
