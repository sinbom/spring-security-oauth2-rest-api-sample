package me.nuguri.auth.entity;

import lombok.*;

import javax.persistence.*;

/**
 * 재발급 토큰 엔티티
 */
@Entity
@Table(name = "oauth_refresh_token")
@Getter
@Setter
@EqualsAndHashCode(of ="id")
public class RefreshToken {

    @Id
    @GeneratedValue
    private Long id;

    private String token_id;

    @Lob
    private Byte[] token;

    @Lob
    private Byte[] authentication;

}
