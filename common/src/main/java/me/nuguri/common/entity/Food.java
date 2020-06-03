package me.nuguri.common.entity;

import lombok.*;
import me.nuguri.common.enums.Size;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@DiscriminatorValue("F")
@PrimaryKeyJoinColumn(name = "product_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Food extends Product {

    /** 칼로리 */
    private int calorie;

    /** 중량 gram */
    private int weightGram;

    @Builder
    protected Food(Long id, int calorie, int weightGram, String name, int price, int stockCount, Creator creator, Company company) {
        super(id, name, price, stockCount, creator, company);
        this.calorie = calorie;
        this.weightGram = weightGram;
    }

}
