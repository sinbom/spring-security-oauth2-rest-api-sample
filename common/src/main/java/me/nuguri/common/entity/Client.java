package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 클라이언트 엔티티
 */
@Entity
@Table(name = "oauth_client_details")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of ="id", callSuper = false)
public class Client extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 클라이언트 Id */
    @Column(updatable = false, unique = true)
    private String clientId;

    /** 리소스 Id */
    @Column(nullable = false)
    private String resourceIds;

    /** 클라이언트 Secret */
    @Column(nullable = false, updatable = false)
    private String clientSecret;

    /** 접근 범위 */
    @Column(nullable = false)
    private String scope;

    /** 권한 부여 방식 */
    @Column(name = "authorizedGrantTypes", nullable = false)
    private String grantTypes;

    /** 리다이렉트 URI */
    @Column(name = "web_server_redirect_uri", nullable = false)
    private String redirectUri;

    /** 권한 */
    @Column(nullable = false)
    private String authorities;

    /** 토근 유효 시간 초 */
    @Column(nullable = false)
    private Integer accessTokenValidity;

    /** 재발급 토큰 유효 시간 초 */
    @Column(nullable = false)
    private Integer refreshTokenValidity;

    /** 토큰 추가 정보 */
    private String additionalInformation;

    /** 인증 동의 자동 저장 여부*/
    private String autoapprove;

    /** 클라이언트 등록 계정 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account account;

    @Builder
    protected Client(Long id, String clientId, String resourceIds, String clientSecret, String scope, String grantTypes, String redirectUri, String authorities,
                     Integer accessTokenValidity, Integer refreshTokenValidity, String additionalInformation, String autoapprove, Account account) {
        this.id = id;
        this.clientId = clientId;
        this.resourceIds = resourceIds;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.grantTypes = grantTypes;
        this.redirectUri = redirectUri;
        this.authorities = authorities;
        this.accessTokenValidity = accessTokenValidity != null ? accessTokenValidity : 600;
        this.refreshTokenValidity = refreshTokenValidity != null ? refreshTokenValidity : 3600;
        this.additionalInformation = additionalInformation;
        this.autoapprove = autoapprove;
        this.addAccount(account);
    }

    /**
     * 양방향 관계 설정
     * @param account 계정
     */
    public void addAccount(Account account) {
        this.account = account;
        this.account.getClients().add(this);
    }

}
