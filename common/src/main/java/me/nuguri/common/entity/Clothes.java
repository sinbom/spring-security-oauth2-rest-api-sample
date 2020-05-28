package me.nuguri.common.entity;

import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.enums.Size;

import javax.persistence.*;

@Entity
@DiscriminatorValue("C")
@PrimaryKeyJoinColumn(name = "clothes_id")
@Getter
@Setter
public class Clothes extends Product {

    /** 사이즈 */
    @Enumerated(EnumType.STRING)
    @Column(nullable =  false)
    private Size size;

}
