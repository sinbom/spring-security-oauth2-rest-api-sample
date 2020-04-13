package me.nuguri.auth.entity;

import lombok.*;
import me.nuguri.auth.common.GrantType;
import me.nuguri.auth.common.Scope;

import javax.persistence.*;
import java.util.Set;

@Entity
@EqualsAndHashCode(of = "clientId")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue
    private Long id;

    private String clientId;

    private String resourceId;

    private String clientSecret;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Scope> scope;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<GrantType> grantType;

    private String redirectUri;

    private String authorities;

    private String accessToken;

    private Long acTkValiditySec;

    private Long rfTkValiditySec;

    private Boolean autoApprove;


}
