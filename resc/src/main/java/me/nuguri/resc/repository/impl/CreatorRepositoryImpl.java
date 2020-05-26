package me.nuguri.resc.repository.impl;

import com.google.common.collect.Iterators;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.resc.domain.CreatorSearchCondition;
import me.nuguri.resc.entity.Creator;
import me.nuguri.resc.entity.QProduct;
import me.nuguri.resc.entity.QProductCategory;
import me.nuguri.resc.enums.Gender;
import me.nuguri.resc.repository.CreatorRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static me.nuguri.resc.entity.QCreator.creator;
import static me.nuguri.resc.entity.QProduct.product;
import static me.nuguri.resc.entity.QProductCategory.productCategory;

@Transactional
@RequiredArgsConstructor
public class CreatorRepositoryImpl implements CreatorRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private final EntityManager entityManager;

    @Override
    public void deleteByIds(Long... ids) {
        List<Tuple> result = jpaQueryFactory
                .select(product.id, productCategory.id)
                .from(creator)
                .leftJoin(creator.products, product)
                .leftJoin(product.productCategories, productCategory)
                .where(creator.id.in(ids))
                .fetch();

        // 제거를 위해  연관 ID 조회 (엔티티 매니저 remove로 제거해도 연관 관계를 따라서 select 쿼리가 N+1로 발생함)
        if (result.size() == 0) {
            throw new NoSuchElementException();
        }

        List<Long> productIds = result.stream().map(t -> t.get(product.id)).collect(Collectors.toList());
        List<Long> productCategoryIds = result.stream().map(t -> t.get(productCategory.id)).collect(Collectors.toList());

        if (!productCategoryIds.isEmpty()) {
            jpaQueryFactory
                    .delete(productCategory)
                    .where(productCategory.id.in(productCategoryIds))
                    .execute();
        }

        if (!productIds.isEmpty()) {
            jpaQueryFactory
                    .delete(product)
                    .where(product.id.in(productIds))
                    .execute();
        }

        jpaQueryFactory
                .delete(creator)
                .where(creator.id.in(ids))
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Creator> findByCondition(CreatorSearchCondition condition, Pageable pageable) {
        QueryResults<Creator> result = jpaQueryFactory
                .selectFrom(creator)
                .where(
                        eqName(condition.getName()),
                        eqGender(condition.getGender()),
                        betweenBirth(condition.getStartBirth(), condition.getEndBirth()),
                        betweenDeath(condition.getStartDeath(), condition.getEndDeath())
                )
                .offset(pageable.getPageNumber())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers(pageable))
                .fetchResults();
        return new PageImpl<>(result.getResults(), pageable, result.getTotal());
    }

    private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable) {
        Sort sort = pageable.getSort();
        Iterator<Sort.Order> iterator = sort.iterator();
        OrderSpecifier<?>[] orderSpecifiers = new OrderSpecifier[Iterators.size(sort.iterator())];

        for (int i = 0; iterator.hasNext(); i++) {
            Sort.Order order = iterator.next();
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();
            SimplePath<Object> path = Expressions.path(Object.class, creator, property);
            orderSpecifiers[i] = new OrderSpecifier(direction, path);
        }

        return orderSpecifiers;
    }

    private BooleanExpression betweenBirth(LocalDate startBirth, LocalDate endBirth) {
        if (startBirth != null && endBirth != null) {
            return creator.birth.between(startBirth, endBirth);
        } else if (startBirth == null && endBirth == null) {
            return null;
        } else {
            return startBirth == null ? creator.birth.loe(endBirth) : creator.birth.goe(startBirth);
        }
    }

    private BooleanExpression betweenDeath(LocalDate startDeath, LocalDate endDeath) {
        if (startDeath != null && endDeath != null) {
            return creator.death.between(startDeath, endDeath);
        } else if (startDeath == null && endDeath == null) {
            return null;
        } else {
            return startDeath == null ? creator.death.loe(endDeath) : creator.death.goe(startDeath);
        }
    }

    private BooleanExpression eqGender(Gender gender) {
        return gender != null ? creator.gender.eq(gender) : null;
    }

    private BooleanExpression eqName(String name) {
        return !StringUtils.isEmpty(name) ? creator.name.eq(name) : null;
    }

}
