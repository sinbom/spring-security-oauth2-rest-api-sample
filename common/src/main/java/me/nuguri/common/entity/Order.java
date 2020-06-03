package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Order extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 배송 (단방향)*/
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Delivery delivery;

    /** 주문자 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account account;

    /** 주문 목록 */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Builder
    protected Order(Long id, Delivery delivery, Account account) {
        this.id = id;
        this.delivery = delivery;
        this.addAccount(account);
    }

    /**
     * 양방향 관계 설정
     * @param account 계정
     */
    public void addAccount(Account account) {
        this.account = account;
        account.getOrders().add(this);
    }

}
