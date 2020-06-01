package me.nuguri.account.repository.impl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.account.controller.dto.AccountSearchCondition;
import me.nuguri.account.repository.AccountRepositoryCustom;
import me.nuguri.account.repository.support.QuerydslSupportCustom;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.QAccount;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.Role;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static me.nuguri.common.entity.QAccount.account;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class AccountRepositoryImpl extends QuerydslSupportCustom implements AccountRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Account> pageByCondition(AccountSearchCondition condition, Pageable pageable) {
        List<Account> content = jpaQueryFactory
                .selectFrom(account)
                .where(
                        eqEmail(condition.getEmail()),
                        eqName(condition.getName()),
                        eqGender(condition.getGender()),
                        eqRole(condition.getRole())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(account, pageable))
                .fetch();

        JPAQuery<Account> countQuery = jpaQueryFactory
                .selectFrom(account);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    private BooleanExpression eqRole(Role role) {
        return role != null ? account.role.eq(role) : null;
    }

    private BooleanExpression eqGender(Gender gender) {
        return gender != null ? account.gender.eq(gender) : null;
    }

    private BooleanExpression eqName(String name) {
        return hasText(name) ? account.name.eq(name) : null;
    }

    private BooleanExpression eqEmail(String email) {
        return hasText(email) ? account.email.eq(email) : null;
    }
}
