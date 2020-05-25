package me.nuguri.resc.entity;

import lombok.Getter;
import lombok.Setter;
import me.nuguri.resc.enums.Size;

import javax.persistence.*;

@Entity
@DiscriminatorValue("C")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
public class Clothes extends Product {

    /** 사이즈 */
    @Enumerated(EnumType.STRING)
    @Column(nullable =  false)
    private Size size;

}
