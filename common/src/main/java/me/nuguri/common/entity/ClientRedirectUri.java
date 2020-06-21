package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;

/**
 * 클라이언트, 리다이렉트 경로 매핑 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class ClientRedirectUri extends BaseEntity {

    /**
     * 식별키
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 리다이렉트 경로
     */
    @Column(nullable = false)
    private String uri;

    /**
     * 클라이언트 엔티티
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @Builder
    protected ClientRedirectUri(Long id, String uri, Client client) {
        this.id = id;
        this.uri = uri;
        this.addClient(client);
    }

    /**
     * 양방향 관계 매핑
     *
     * @param client 클라이언트
     */
    public void addClient(Client client) {
        this.client = client;
        client.getClientRedirectUris().add(this);
    }

}
