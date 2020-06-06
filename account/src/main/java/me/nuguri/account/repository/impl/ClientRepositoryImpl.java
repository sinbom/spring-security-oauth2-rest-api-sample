package me.nuguri.account.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.account.dto.ClientSearchCondition;
import me.nuguri.account.repository.ClientRepositoryCustom;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import me.nuguri.common.support.QuerydslSupportCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static me.nuguri.common.entity.QAccount.account;
import static me.nuguri.common.entity.QClient.client;
import static org.springframework.util.StringUtils.hasText;

@Transactional
@RequiredArgsConstructor
public class ClientRepositoryImpl extends QuerydslSupportCustom implements ClientRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 클라이언트 엔티티, 유저 엔티티 패치 조인 페이지 조회
     * @param condition
     * @param pageable page 페이지, size 사이즈, sort 정렬
     * @return 조회한 클라이언트 엔티티 페이징 객체
     */
    @Override
    public Page<Client> pageByConditionFetchAccounts(ClientSearchCondition condition, Pageable pageable) {
        JPAQuery<Client> countQuery = jpaQueryFactory
                .selectFrom(client)
                .innerJoin(client.account, account)
                .fetchJoin()
                .where(
                        eqClientId(condition.getClientId()),
                        eqResourceIds(condition.getResourceId()),
                        eqScope(condition.getScope()),
                        eqGrantType(condition.getGrantType()),
                        eqRedirectUri(condition.getRedirectUri()),
                        eqAuthority(condition.getAuthority()),
                        eqEmail(condition.getEmail()),
                        betweenCreated(client, condition.getStartCreated(), condition.getEndCreated()),
                        betweenUpdated(client, condition.getStartUpdated(), condition.getEndUpdated())
                );
        List<Client> content = countQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(client, pageable))
                .fetch();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    @Override
    public long deleteByIdsBatchInQuery(List<Long> ids) {
        return 0;
    }

    private BooleanExpression eqEmail(String email) {
        return hasText(email) ? client.account.email.eq(email) : null;
    }

    private BooleanExpression eqAuthority(Role authority) {
        return authority != null ? client.authority.eq(authority) : null;
    }

    private BooleanExpression eqRedirectUri(String redirectUri) {
        return hasText(redirectUri) ? client.redirectUri.eq(redirectUri) : null;
    }

    private BooleanExpression eqGrantType(GrantType grantType) {
        return grantType != null ? client.grantTypes.eq(grantType.toString()) : null;
    }

    private BooleanExpression eqScope(Scope scope) {
        return scope != null ? client.scope.containsIgnoreCase(scope.toString()) : null;
    }

    private BooleanExpression eqResourceIds(String resourceIds) {
        return client.resourceIds.eq(resourceIds);
    }

    private BooleanExpression eqClientId(String clientId) {
        return client.clientId.eq(clientId);
    }
}
