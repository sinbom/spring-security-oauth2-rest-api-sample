package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 상위 카테고리 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Category extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 부모 카테고리 매핑 */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Category category;

    /** 자식 카테고리 */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Category> subCategories = new ArrayList<>();


    /** 카테고리, 책 매핑 */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @Builder
    protected Category(Long id, String name, Category category) {
        this.id = id;
        this.name = name;
        if (category != null) { // optional true인 연관 관계시 null 체크 필요
            this.addCategory(category);
        }
    }

    /**
     * 양방향 관계 설정
     * @param category 카테고리
     */
    public void addCategory(Category category) {
        this.category = category;
        category.getSubCategories().add(this);
    }

}
