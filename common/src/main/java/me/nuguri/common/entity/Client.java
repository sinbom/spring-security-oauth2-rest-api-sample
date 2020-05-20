package me.nuguri.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 클라이언트 엔티티
 */
@Entity
@Table(name = "oauth_client_details")
@Getter
@Setter
@EqualsAndHashCode(of ="clientId")
public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 클라이언트 Id */
    @Id
    @Column(updatable = false)
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
    private Integer accessTokenValidity = 600;

    /** 재발급 토큰 유효 시간 초 */
    @Column(nullable = false)
    private Integer refreshTokenValidity = 3600;

    /** 토큰 추가 정보 */
    private String additionalInformation;

    /** 인증 동의 자동 저장 여부*/
    private String autoapprove;

    /** 클라이언트 등록 계정 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account account;

    public void addAccount(Account account) {
        this.account = account;
        this.account.getClients().add(this);
    }

}
