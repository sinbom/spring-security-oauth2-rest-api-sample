package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;

/**
 * 상품, 주문 매핑 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class OrderProduct {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 슈량 */
    private int count;

    /** 상품 (단방향)*/
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    /** 주문 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Order order;

    @Builder
    protected OrderProduct(Long id, int count, Product product, Order order) {
        this.id = id;
        this.count = count;
        this.product = product;
        this.addOrder(order);
    }

    /**
     * 양방향 관계 설정
     * @param order 주문
     */
    public void addOrder(Order order) {
        this.order = order;
        order.getOrderProducts().add(this);
    }

}
