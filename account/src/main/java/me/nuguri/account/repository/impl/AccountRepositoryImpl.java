package me.nuguri.account.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.account.dto.AccountSearchCondition;
import me.nuguri.account.repository.AccountRepositoryCustom;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Address;
import me.nuguri.common.entity.QAccount;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.Role;
import me.nuguri.common.support.QuerydslSupportCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static me.nuguri.common.entity.QAccount.account;
import static me.nuguri.common.entity.QClient.client;
import static org.springframework.util.StringUtils.hasText;

@Transactional
@RequiredArgsConstructor
public class AccountRepositoryImpl extends QuerydslSupportCustom implements AccountRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 유저 엔티티 페이지 조회
     * @param condition
     * @param pageable page 페이지, size 사이즈, sort 정렬
     * @return 조회한 유저 엔티티 페이징 객체
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Account> pageByCondition(AccountSearchCondition condition, Pageable pageable) {
        List<Account> content = jpaQueryFactory
                .selectFrom(account)
                .where(
                        eqEmail(condition.getEmail()),
                        eqName(condition.getName()),
                        eqGender(condition.getGender()),
                        eqRole(condition.getRole()),
                        eqAddress(condition.getAddress()),
                        betweenCreated(account, condition.getStartCreated(), condition.getEndCreated()),
                        betweenUpdated(account, condition.getStartUpdated(), condition.getEndUpdated())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(account, pageable))
                .fetch();

        JPAQuery<Account> countQuery = jpaQueryFactory
                .selectFrom(account);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 유저 엔티티 조회, 대리키(email) 조회, 클라이언트 정보 패치 조인 조회
     *
     * @param email 이메일
     * @return 조회한 유저 엔티티 객체
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<Account> findByEmailFetchClients(String email) {
        Account result = jpaQueryFactory
                .selectFrom(account)
                .leftJoin(account.clients, client)
                .fetchJoin()
                .where(eqEmail(email))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    private BooleanExpression eqAddress(Address address) {
        return address != null ? account.address.eq(address) : null;
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
