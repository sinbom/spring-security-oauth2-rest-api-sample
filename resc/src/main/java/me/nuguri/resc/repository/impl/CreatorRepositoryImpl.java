package me.nuguri.resc.repository.impl;

import com.google.common.collect.Iterators;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.resc.domain.CreatorSearchCondition;
import me.nuguri.resc.entity.Creator;
import me.nuguri.resc.enums.Gender;
import me.nuguri.resc.repository.CreatorRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Predicates.notNull;
import static java.util.stream.Collectors.toList;
import static me.nuguri.resc.entity.QCreator.creator;
import static me.nuguri.resc.entity.QProduct.product;
import static me.nuguri.resc.entity.QProductCategory.productCategory;

@Transactional
@RequiredArgsConstructor
public class CreatorRepositoryImpl implements CreatorRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void deleteByIds(List<Long> ids) {
        // 저자와 연관된 상품 엔티티 조회
        List<Tuple> result = jpaQueryFactory
                .select(creator.id, product.id, productCategory.id)
                .from(creator)
                .leftJoin(creator.products, product)
                .leftJoin(product.productCategories, productCategory)
                .where(creator.id.in(ids))
                .fetch();

        List<Long> creatorIds = result
                .stream()
                .map(t -> t.get(creator.id))
                .filter(Objects::nonNull)
                .distinct()
                .collect(toList());

        List<Long> productIds = result
                .stream()
                .map(t -> t.get(product.id))
                .filter(Objects::nonNull)
                .distinct()
                .collect(toList());

        List<Long> productCategoryIds = result
                .stream()
                .map(t -> t.get(productCategory.id))
                .filter(Objects::nonNull)
                .collect(toList());

        // 상품 카테고리 엔티티 삭제
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

        if (!creatorIds.isEmpty()) {
            // 저자 엔티티 삭제
            jpaQueryFactory
                    .delete(creator)
                    .where(creator.id.in(ids))
                    .execute();
        }

        /*if (!result.isEmpty()) {
            Map<? extends Class<? extends Product>, List<Product>> group = result
                    .stream()
                    .collect(Collectors.groupingBy(Product::getClass));

            // 상품 카테고리 엔티티 삭제
            List<Long> allProductsIds = result
                    .stream()
                    .map(Product::getId)
                    .collect(toList());
            jpaQueryFactory
                    .delete(productCategory)
                    .where(productCategory.product.id.in(allProductsIds))
                    .execute();

            // 상품 엔티티 삭제
            for (Class<? extends Product> key : group.keySet()) {
                List<Product> products = group.get(key);
                List<Long> productIds = products
                        .stream()
                        .map(Product::getId)
                        .collect(toList());
                PathBuilder<? extends Product> path = new PathBuilder<>(key, key.getName());
                jpaQueryFactory
                        .delete(path)
                        .where(product.id.in(productIds))
                        .execute();
            }
        }
        // 저자 엔티티 삭제
        jpaQueryFactory
                .delete(creator)
                .where(creator.id.in(ids))
                .execute();*/
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Creator> findByCondition(CreatorSearchCondition condition, Pageable pageable) {
        // 페이징 쿼리
        List<Creator> content = jpaQueryFactory
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
                .fetch();

        // 카운트 쿼리
        JPAQuery<Creator> countQuery = jpaQueryFactory
                .selectFrom(creator)
                .where(
                        eqName(condition.getName()),
                        eqGender(condition.getGender()),
                        betweenBirth(condition.getStartBirth(), condition.getEndBirth()),
                        betweenDeath(condition.getStartDeath(), condition.getEndDeath())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
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
