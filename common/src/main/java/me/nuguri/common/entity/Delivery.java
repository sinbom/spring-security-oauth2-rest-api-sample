package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.enums.DeliveryStatus;

import javax.persistence.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Delivery {

    /** 식별 키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 배송 상태 */
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.READY;

    /** 주소 */
    @Embedded
    private Address address;

}
