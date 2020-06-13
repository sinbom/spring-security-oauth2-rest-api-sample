package me.nuguri.account.repository.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.account.dto.AccountSearchCondition;
import me.nuguri.account.repository.AccountRepositoryCustom;
import me.nuguri.common.entity.Account;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.Role;
import me.nuguri.common.support.QuerydslSupportCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static me.nuguri.common.entity.QAccount.account;
import static me.nuguri.common.entity.QClient.client;
import static me.nuguri.common.entity.QDelivery.delivery;
import static me.nuguri.common.entity.QOrder.order;
import static me.nuguri.common.entity.QOrderProduct.orderProduct;
import static org.springframework.util.StringUtils.hasText;

@Transactional
@RequiredArgsConstructor
public class AccountRepositoryImpl extends QuerydslSupportCustom implements AccountRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 유저 엔티티 페이지 조회
     *
     * @param condition
     * @param pageable  page 페이지, size 사이즈, sort 정렬
     * @return 조회한 유저 엔티티 페이징 객체
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Account> pageByCondition(AccountSearchCondition condition, Pageable pageable) {
        JPAQuery<Account> countQuery = jpaQueryFactory
                .selectFrom(account)
                .where(
                        eqEmail(condition.getEmail()),
                        eqName(condition.getName()),
                        eqGender(condition.getGender()),
                        eqRole(condition.getRole()),
                        eqCity(condition.getCity()),
                        eqStreet(condition.getStreet()),
                        eqZipCode(condition.getZipCode()),
                        betweenCreated(account, condition.getStartCreated(), condition.getEndCreated()),
                        betweenUpdated(account, condition.getStartUpdated(), condition.getEndUpdated())
                );
        List<Account> content = countQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(account, pageable))
                .fetch();
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

    @Override
    public long deleteByIdsBatchInQuery(List<Long> ids) {
        // 삭제 유저 엔티티와 연관 관계에 있는 엔티티의 식별키 조회
        List<Tuple> result = jpaQueryFactory
                .select(order.id, order.delivery.id)
                .from(order)
                .where(order.account.id.in(ids))
                .fetch();
        // 삭제 요청 받은 유저의 주문 엔티티 식별키
        List<Long> orderIds = result
                .stream()
                .map(t -> t.get(order.id))
                .collect(toList());
        // 삭제 요청 받은 유저의 배송 엔티티 식별키
        List<Long> deliveryIds = result
                .stream()
                .map(t -> t.get(order.delivery.id))
                .collect(toList());
        // 배송 엔티티 제거
        if (!deliveryIds.isEmpty()) {
            jpaQueryFactory
                    .delete(delivery)
                    .where(delivery.id.in(deliveryIds))
                    .execute();
        }
        // 주문 상품 엔티티 제거
        if (!orderIds.isEmpty()) {
            jpaQueryFactory
                    .delete(orderProduct)
                    .where(orderProduct.order.id.in(orderIds))
                    .execute();
        }
        // 주문 엔티티 제거
        if (!orderIds.isEmpty()) {
            jpaQueryFactory
                    .delete(order)
                    .where(order.id.in(orderIds))
                    .execute();
        }
        // 클라이언트 엔티티 제거
        jpaQueryFactory
                .delete(client)
                .where(client.account.id.in(ids))
                .execute();
        // 유저 엔티티 제거
        long count = jpaQueryFactory
                .delete(account)
                .where(inIds(ids))
                .execute();
        if (count < 1) {
            throw new EntityNotFoundException();
        }
        return count;
    }

    private BooleanExpression inIds(List<Long> ids) {
        return account.id.in(ids);
    }

    private BooleanExpression eqCity(String city) {
        return hasText(city) ? account.address.city.eq(city) : null;
    }

    private BooleanExpression eqStreet(String street) {
        return hasText(street) ? account.address.street.eq(street) : null;
    }

    private BooleanExpression eqZipCode(String zipCode) {
        return hasText(zipCode) ? account.address.zipCode.eq(zipCode) : null;
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
