package me.nuguri.auth.domain;

import me.nuguri.auth.controller.AuthorizationServerController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class AccessTokenResource extends EntityModel<OAuth2AccessToken> {

    public AccessTokenResource(OAuth2AccessToken content, Link... links) {
        super(content, links);
        add(linkTo(methodOn(AuthorizationServerController.class).revokeToken(null)).withSelfRel());
        add(linkTo(AuthorizationServerController.class).slash("docs/index.html").withRel("document"));
    }
}
