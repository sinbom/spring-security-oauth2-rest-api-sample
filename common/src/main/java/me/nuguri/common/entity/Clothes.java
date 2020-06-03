package me.nuguri.common.entity;

import lombok.*;
import me.nuguri.common.enums.Size;

import javax.persistence.*;

@Entity
@DiscriminatorValue("C")
@PrimaryKeyJoinColumn(name = "product_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Clothes extends Product {

    /** 사이즈 */
    @Enumerated(EnumType.STRING)
    @Column(nullable =  false)
    private Size size;

    @Builder
    protected Clothes(Long id, Size size, String name, int price, int stockCount, Creator creator, Company company) {
        super(id, name, price, stockCount, creator, company);
        this.size = size;
    }

}
