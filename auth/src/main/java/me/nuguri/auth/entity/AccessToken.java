package me.nuguri.auth.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "oauth_access_token")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of ="client_id")
@Builder
public class AccessToken {

    @Id
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
