package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Order extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 배송 */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Delivery delivery;

    /** 주문자 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account account;

    /** 주문 목록 */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    /**
     * 양방향 관계 설정
     * @param account 계정
     */
    public void addAccount(Account account) {
        this.account = account;
        account.getOrders().add(this);
    }

}
