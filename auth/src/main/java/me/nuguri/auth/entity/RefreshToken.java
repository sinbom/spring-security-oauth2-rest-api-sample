package me.nuguri.auth.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "oauth_refresh_token")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of ="token_id")
@Builder
public class RefreshToken {

    @Id
    private String token_id;

    @Lob
    private Byte[] token;

    @Lob
    private Byte[] authentication;

}
