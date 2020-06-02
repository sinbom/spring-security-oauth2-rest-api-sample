package me.nuguri.common.support;

import com.google.common.collect.Iterators;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;

public abstract class QuerydslSupportCustom {

    protected OrderSpecifier<?>[] getOrderSpecifiers(EntityPathBase<?> entityPathBase, Pageable pageable) {
        Sort sort = pageable.getSort();
        Iterator<Sort.Order> iterator = sort.iterator();
        OrderSpecifier<?>[] orderSpecifiers = new OrderSpecifier[Iterators.size(sort.iterator())];

        for (int i = 0; iterator.hasNext(); i++) {
            Sort.Order order = iterator.next();
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();
            SimplePath<Object> path = Expressions.path(Object.class, entityPathBase, property);
            orderSpecifiers[i] = new OrderSpecifier(direction, path);
        }

        return orderSpecifiers;
    }

    protected BooleanExpression betweenCreated(EntityPathBase<?> entityPathBase, LocalDate startDate, LocalDate endDate) {
        TimePath<LocalDateTime> created = Expressions.timePath(LocalDateTime.class, entityPathBase, "created");
        return betweenDateTime(created, startDate, endDate);
    }

    protected BooleanExpression betweenUpdated(EntityPathBase<?> entityPathBase, LocalDate startDate, LocalDate endDate) {
        TimePath<LocalDateTime> updated = Expressions.timePath(LocalDateTime.class, entityPathBase, "updated");
        return betweenDateTime(updated, startDate, endDate);
    }

    private BooleanExpression betweenDateTime(TimePath<LocalDateTime> timePath, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return timePath.between(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        } else if (startDate == null && endDate == null) {
            return null;
        } else {
            return startDate == null ? timePath.loe(endDate.atTime(LocalTime.MAX)) : timePath.goe(startDate.atStartOfDay());
        }
    }



}
