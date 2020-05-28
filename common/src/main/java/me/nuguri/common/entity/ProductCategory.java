package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 카테고리, 책 매핑 엔티티
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class ProductCategory extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 카테고리 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private MinorCategory category;

    /** 책 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    /**
     * 양방향 관계 설정
     * @param category 카테고리
     */
    public void addMinorCategory(MinorCategory category) {
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
