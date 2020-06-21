package me.nuguri.common.entity;

import lombok.*;
import me.nuguri.common.enums.GrantType;

import javax.persistence.*;

/**
 * 클라이언트, 인증 부여 방식 매핑 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class ClientGrantType extends BaseEntity {

    /**
     * 식별키
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 인증 부여 방식
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrantType grantType;

    /**
     * 클라이언트 엔티티
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @Builder
    protected ClientGrantType(Long id, GrantType grantType, Client client) {
        this.id = id;
        this.grantType = grantType;
        this.addClient(client);
    }

    /**
     * 양방향 관계 설정
     * @param client 클라이언트
     */
    public void addClient(Client client) {
        this.client = client;
        client.getClientGrantTypes().add(this);
    }

}
