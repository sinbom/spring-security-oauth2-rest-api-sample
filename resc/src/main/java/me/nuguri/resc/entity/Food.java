package me.nuguri.resc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@DiscriminatorValue("F")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
public class Food extends Product {

    /** 칼로리 */
    private int calorie;

    /** 중량 gram */
    private int weightGram;

}
