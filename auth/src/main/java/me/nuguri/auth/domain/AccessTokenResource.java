package me.nuguri.auth.domain;

import me.nuguri.auth.controller.AuthorizationServerController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class AccessTokenResource extends EntityModel<AccessToken> {

    public AccessTokenResource(AccessToken content, Link... links) {
        super(content, links);
        add(linkTo(methodOn(AuthorizationServerController.class).revokeToken(null)).withSelfRel());
        add(linkTo(AuthorizationServerController.class).slash("docs/index.html").withRel("document"));
    }
}
