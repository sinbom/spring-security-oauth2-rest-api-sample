package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 상품, 주문 매핑 엔티티
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class OrderProduct {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 슈량 */
    private int count;

    /** 상품 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    /** 주문 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Order order;

    /**
     * 양방향 관계 설정
     * @param order 주문
     */
    public void addOrder(Order order) {
        this.order = order;
        order.getOrderProducts().add(this);
    }

}
