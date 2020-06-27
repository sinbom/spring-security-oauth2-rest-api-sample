package me.nuguri.auth.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.auth.repository.custom.ClientRepositoryCustom;
import me.nuguri.common.entity.*;
import me.nuguri.common.support.QuerydslSupportCustom;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static me.nuguri.common.entity.QAuthority.authority;
import static me.nuguri.common.entity.QClient.client;
import static me.nuguri.common.entity.QClientAuthority.clientAuthority;
import static me.nuguri.common.entity.QClientResource.clientResource;
import static me.nuguri.common.entity.QClientScope.clientScope;
import static me.nuguri.common.entity.QResource.resource;
import static me.nuguri.common.entity.QScope.scope;
import static org.springframework.util.StringUtils.hasText;

@Transactional
@RequiredArgsConstructor
public class ClientRepositoryImpl extends QuerydslSupportCustom implements ClientRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Transactional(readOnly = true)
    @Override
    public Optional<Client> findByClientIdFetchAuthority(String clientId) {
        Client c = jpaQueryFactory
                .selectFrom(client)
                .leftJoin(client.clientAuthorities, clientAuthority)
                .fetchJoin()
                .innerJoin(clientAuthority.authority, authority)
                .fetchJoin()
                .where(eqClientId(clientId))
                .fetchOne();
        return Optional.ofNullable(c);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientScope> findClientScopesByIdFetchScopes(Long id) {
        return jpaQueryFactory
                .selectFrom(clientScope)
                .innerJoin(clientScope.scope, scope)
                .fetchJoin()
                .where(clientScope.client.id.eq(id))
                .fetch();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientResource> findClientResourcesByIdFetchResources(Long id) {
        return jpaQueryFactory
                .selectFrom(clientResource)
                .innerJoin(clientResource.resource, resource)
                .fetchJoin()
                .where(clientResource.client.id.eq(id))
                .fetch();
    }

    private BooleanExpression eqClientId(String clientId) {
        return hasText(clientId) ? client.clientId.eq(clientId) : null;
    }

}
