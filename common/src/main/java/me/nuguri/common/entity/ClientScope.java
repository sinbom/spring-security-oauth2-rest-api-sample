package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;

/**
 * 클라이언트, 접근 범위 매핑 엔티티
 */
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
     * 클라이언트 접근 범위 엔티티, 단방향
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Scope scope;

    @Builder
    protected ClientScope(Long id, Client client, Scope scope) {
        this.id = id;
        this.addClient(client);
        this.scope = scope;
    }

    /**
     * 양방향 관계 설정
     * @param client 클라이언트
     */
    public void addClient(Client client) {
        this.client = client;
        client.getClientScopes().add(this);
    }

}
