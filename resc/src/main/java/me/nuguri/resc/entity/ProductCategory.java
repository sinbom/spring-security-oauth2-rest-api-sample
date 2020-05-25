package me.nuguri.resc.entity;

import lombok.*;
import me.nuguri.resc.enums.Gender;

import javax.persistence.*;

/**
 * 카테고리, 책 매핑 엔티티
 */
@Entity
@Getter
@Setter
public class ProductCategory extends BaseEntity {

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
