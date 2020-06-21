package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class ClientScope extends BaseEntity {

    /**
     * 식별키
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 클라이언트 엔티티
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    /**
     * 클라이언트 접근 범위 엔티티
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Scope scope;

}
