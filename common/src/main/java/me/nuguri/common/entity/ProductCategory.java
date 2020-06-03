package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;

/**
 * 카테고리, 책 매핑 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class ProductCategory extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 카테고리 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Category category;

    /** 책 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    @Builder
    protected ProductCategory(Long id, Category category, Product product) {
        this.id = id;
        this.addCategory(category);
        this.addProduct(product);
    }

    /**
     * 양방향 관계 설정
     * @param category 카테고리
     */
    public void addCategory(Category category) {
        this.category = category;
        category.getProductCategories().add(this);
    }

    /**
     * 양방향 관계 설정
     * @param product 상품
     */
    public void addProduct(Product product) {
        this.product = product;
        product.getProductCategories().add(this);
    }

}
