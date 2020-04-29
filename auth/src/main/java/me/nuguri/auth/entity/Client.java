package me.nuguri.auth.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "oauth_client_details")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of ="client_id")
@Builder
public class Client {

    @Id
    private String client_id;

    private String resource_ids;

    private String client_secret;

    private String scope;

    private String authorized_grant_types;

    private String web_server_redirect_uri;

    private String authorities;

    private Integer access_token_validity = 600;

    private Integer refresh_token_validity = 3600;

    private String additional_information;

    private String autoapprove;

}
