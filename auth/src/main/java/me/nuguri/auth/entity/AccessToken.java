package me.nuguri.auth.entity;

import lombok.*;

import javax.persistence.*;

/**
 * 엑세스 토큰 엔티티
 */
@Entity
@Table(name = "oauth_access_token")
@Getter
@Setter
@EqualsAndHashCode(of ="id")
public class AccessToken {

    @Id
    @GeneratedValue
    private Long id;

    private String authentication_id;

    private String token_id;

    private String user_name;

    private String client_id;

    @Lob
    private Byte[] token;

    @Lob
    private Byte[] authentication;

    private String refresh_token;

}
