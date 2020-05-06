package me.nuguri.auth.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 클라이언트 엔티티
 */
@Entity
@Table(name = "oauth_client_details")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of ="client_id")
@Builder
public class Client {

    /** 클라이언트 Id */
    @Id
    private String client_id;

    /** 리소스 Id */
    private String resource_ids;

    /** 클라이언트 Secret */
    private String client_secret;

    /** 접근 범위 */
    private String scope;

    /** 권한 부여 방식 */
    private String authorized_grant_types;

    /** 리다이렉트 URI */
    private String web_server_redirect_uri;

    /** 권한 */
    private String authorities;

    /** 토근 유효 시간 초 */
    private Integer access_token_validity = 600;

    /** 재발급 토큰 유효 시간 초 */
    private Integer refresh_token_validity = 3600;

    /** 토큰 추가 정보 */
    private String additional_information;

    /** 인증 동의 자동 저장 여부*/
    private String autoapprove;

}
