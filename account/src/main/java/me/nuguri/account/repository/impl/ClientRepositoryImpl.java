package me.nuguri.account.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.account.dto.ClientSearchCondition;
import me.nuguri.account.repository.ClientRepositoryCustom;
import me.nuguri.common.entity.Client;
import me.nuguri.common.entity.QAccount;
import me.nuguri.common.support.QuerydslSupportCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static me.nuguri.common.entity.QAccount.account;
import static me.nuguri.common.entity.QClient.client;

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
                        eqClientId(condition),
                        eqResourceIds(condition),
                        eqScope(condition),
                        eqGrantTypes(condition),
                        eqRedirectUri(condition),
                        client.authorities.eq(condition.getAuthorities()),
                        client.account.email.eq(condition.getEmail()),
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

    private BooleanExpression eqRedirectUri(ClientSearchCondition condition) {
        return client.redirectUri.eq(condition.getRedirectUri());
    }

    private BooleanExpression eqGrantTypes(ClientSearchCondition condition) {
        return client.grantTypes.eq(condition.getGrantTypes());
    }

    private BooleanExpression eqScope(ClientSearchCondition condition) {
        return client.scope.eq(condition.getScope().toString());
    }

    private BooleanExpression eqResourceIds(ClientSearchCondition condition) {
        return client.resourceIds.eq(condition.getResourceIds());
    }

    private BooleanExpression eqClientId(ClientSearchCondition condition) {
        return client.clientId.eq(condition.getClientId());
    }
}
