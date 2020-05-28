package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 하위 카테고리 엔티티
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class MinorCategory extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 상위 카테고리 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private MajorCategory majorCategory;

    /** 카테고리, 책 매핑 */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<ProductCategory> productCategories = new ArrayList<>();

    /**
     * 양방향 관계 설정
     * @param category 카테고리
     */
    public void addMajorCategory(MajorCategory category) {
        this.majorCategory = category;
        category.getMinorCategories().add(this);
    }

}
