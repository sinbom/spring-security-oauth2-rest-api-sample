package me.nuguri.resc.repository.impl;

import com.google.common.collect.Iterators;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.resc.domain.CreatorSearchCondition;
import me.nuguri.resc.entity.Creator;
import me.nuguri.resc.entity.Product;
import me.nuguri.resc.enums.Gender;
import me.nuguri.resc.repository.CreatorRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static me.nuguri.resc.entity.QCreator.creator;
import static me.nuguri.resc.entity.QProduct.product;
import static me.nuguri.resc.entity.QProductCategory.productCategory;

@Transactional
@RequiredArgsConstructor
public class CreatorRepositoryImpl implements CreatorRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long deleteByIds(List<Long> ids) {
        // 저자와 연관된 상품 엔티티, 상품 카테고리 식별키 조회
        List<Tuple> result = jpaQueryFactory
                .select(creator.id, product, productCategory.id)
                .from(creator)
                .leftJoin(creator.products, product)
                .leftJoin(product.productCategories, productCategory)
                .where(creator.id.in(ids))
                .fetch();

        // creator 조인을 줄일 수 있지만 조인 하나 더한다고 로우가 많이 늘어나서 성능 저하가 클 것 같지만
        // 성능이 저하가 크지 않고 오히려 조인을 줄이고 product.creator.id 조회보다 더 빠름
        // PK 조회와 FK 조회의 컬럼 위치가 인덱스 조회에 영향을 미치는 듯?
        // 하지만 조회 로우수가 증가 하므로 속도와 조회량에 맞춰서 쿼리를 사용하면 될 것 같음
/*          List<Tuple> result = jpaQueryFactory
                .select(product.creator.id, product, productCategory.id)
                .from(product)
                .leftJoin(product.productCategories, productCategory)
                .where(product.creator.id.in(ids))
                .fetch();*/

        // 삭제 요청받은 저자 식별키들 중에서 실제로 존재하는 식별키 추출
        List<Long> creatorIds = result
                .stream()
                .map(t -> t.get(creator.id))
                .distinct()
                .collect(toList());

        if (creatorIds.isEmpty()) {
            return 0;
        }

        // 조회한 상품 엔티티를 ptype 기준으로 그룹핑
        Map<? extends Class<? extends Product>, List<Product>> productGroups = result
                .stream()
                .map(t -> t.get(product))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.groupingBy(Product::getClass));

        // 삭제 요청 받은 저자 식별키와 연관된 상품 카테고리 식별키 추출
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

        // 상품 카테고리 엔티티 삭제
        if (!productGroups.isEmpty()) {
            for (Class<? extends Product> ptype : productGroups.keySet()) {
                List<Long> productIds = productGroups
                        .get(ptype)
                        .stream()
                        .map(Product::getId)
                        .collect(toList());
                PathBuilder<? extends Product> entityPath = new PathBuilder<>(ptype, "ptype");

                jpaQueryFactory
                        .delete(entityPath)
                        .where(entityPath.get("id").in(productIds))
                        .execute();
            }
        }

        // 저자 엔티티 삭제
        return jpaQueryFactory
                .delete(creator)
                .where(creator.id.in(creatorIds))
                .execute();
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

    /**
     * pageable 값에 따라 동적으로 querydsl 정렬 조건 생성 메소드
     *
     * @param pageable page 페이지, size 사이즈, sort 정렬
     * @return
     */
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
