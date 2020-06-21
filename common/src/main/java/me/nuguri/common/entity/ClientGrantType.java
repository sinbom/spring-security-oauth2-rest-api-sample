package me.nuguri.common.entity;

import lombok.*;
import me.nuguri.common.enums.GrantType;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class ClientGrantType {

    /**
     * 식별키
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 인증 부여 방식
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrantType grantType;

    /**
     * 클라이언트 엔티티
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

}
