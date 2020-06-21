package me.nuguri.common.entity;

import lombok.*;
import me.nuguri.common.converter.BooleanColumnConverter;
import me.nuguri.common.enums.Size;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 클라이언트 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Client extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 식별키
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 클라이언트 Id
     */
    @Column(unique = true)
    private String clientId;

    /**
     * 클라이언트 Secret
     */
    @Column(updatable = false)
    private String clientSecret;

    /**
     * 토근 유효 시간 초
     */
    @Column(nullable = false)
    private Integer accessTokenValidity;

    /**
     * 재발급 토큰 유효 시간 초
     */
    @Column(nullable = false)
    private Integer refreshTokenValidity;

    /**
     * 인증 동의 자동 저장 여부
     */
    @Convert(converter = BooleanColumnConverter.class)
    @Column(nullable = false)
    private boolean autoApprove;

    /**
     * 클라이언트 등록 계정
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account account;

    /**
     * 클라이언트 접근 권한 목록 엔티티
     */
    @OneToMany(mappedBy = "client")
    private List<ClientAuthority> clientAuthorities = new ArrayList<>();

    /**
     * 클라이언트 접근 범위 목록 엔티티
     */
    @OneToMany(mappedBy = "client")
    private List<ClientScope> clientScopes = new ArrayList<>();

    /**
     * 클라이언트 접근 리소스 목록 엔티티
     */
    @OneToMany(mappedBy = "client")
    private List<ClientResource> clientResources = new ArrayList<>();

    /**
     * 클라이언트 인증 부여 방식 목록 엔티티
     */
    @OneToMany(mappedBy = "client")
    private List<ClientGrantType> clientGrantTypes = new ArrayList<>();

    /**
     * 클라이언트 리다이렉트 목록 엔티티
     */
    @OneToMany(mappedBy = "client")
    private List<ClientRedirectUri> clientRedirectUris = new ArrayList<>();

    @Builder
    protected Client(Long id, String clientId, String clientSecret, Integer accessTokenValidity,
                     Integer refreshTokenValidity, boolean autoApprove, Account account) {
        this.id = id;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessTokenValidity = accessTokenValidity != null ? accessTokenValidity : 600;
        this.refreshTokenValidity = refreshTokenValidity != null ? refreshTokenValidity : 3600;
        this.autoApprove = autoApprove;
        this.addAccount(account);
    }

    /**
     * 양방향 관계 설정
     *
     * @param account 계정
     */
    public void addAccount(Account account) {
        this.account = account;
        this.account.getClients().add(this);
    }

}
