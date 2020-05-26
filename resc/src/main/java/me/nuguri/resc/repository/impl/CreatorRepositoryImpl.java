package me.nuguri.resc.repository.impl;

import com.google.common.collect.Iterators;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.resc.domain.CreatorSearchCondition;
import me.nuguri.resc.entity.Creator;
import me.nuguri.resc.entity.QCreator;
import me.nuguri.resc.enums.Gender;
import me.nuguri.resc.repository.custom.CreatorRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Iterator;

import static me.nuguri.resc.entity.QCreator.creator;

@RequiredArgsConstructor
public class CreatorRepositoryImpl implements CreatorRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private final EntityManager entityManager;

    @Override
    public Page<Creator> findWithCondition(CreatorSearchCondition condition, Pageable pageable) {
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
