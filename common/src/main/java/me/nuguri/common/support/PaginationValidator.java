package me.nuguri.common.support;

import me.nuguri.common.dto.PageableCondition;
import me.nuguri.common.exception.InvalidRequestException;
import me.nuguri.common.exception.NoElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.validation.Errors;

import static org.springframework.util.StringUtils.hasText;

public class PaginationValidator {

    /**
     * Pagination 도메인 condition 값 중 페이지, 페이지 사이즈, 정렬 방식 검증
     *
     * @param pageableCondition page 페이지 번호, size 페이지 사이즈, sort 정렬 방식
     * @param entityType        페이징 객체 엔티티 타입 클래스
     * @param errors            에러
     * @param <T>               페이징 객체 엔티티 타입
     */
    public <T> void validate(PageableCondition pageableCondition, Class<T> entityType, Errors errors) {
        if (hasText(pageableCondition.getSort())) {
            String[] sort = pageableCondition.getSort().split(",");
            if (sort.length > 1) {
                for (int i = 0; i < sort.length - 1; i++) {
                    String property = sort[i];
                    try {
                        entityType.getDeclaredField(property);
                    } catch (NoSuchFieldException e) {
                        errors.rejectValue("sort", "wrongValue", "sort property is wrong");
                    }
                }
                if (!sort[sort.length - 1].equalsIgnoreCase(Sort.Direction.ASC.name()) &&
                        !sort[sort.length - 1].equalsIgnoreCase(Sort.Direction.DESC.name())) {
                    errors.rejectValue("sort", "wrongValue", "sort direction is wrong");
                }
            } else if (sort.length == 1) {
                try {
                    entityType.getDeclaredField(sort[0]);
                } catch (NoSuchFieldException e) {
                    errors.rejectValue("sort", "wrongValue", "sort property is wrong");
                }
            }
        }

        if (errors.hasErrors()) {
            throw new InvalidRequestException(errors, "invalid request parameters");
        }
    }

    /**
     * 페이징 결과 데이터가 없는 경우 예외 발생
     * @param page 페이징 결과
     */
    public void checkEmpty(Page<?> page) {
        if (page.getTotalElements() < 1) {
            long totalElements = page.getTotalElements();
            String message = totalElements < 1 ? "content of all pages does not exist" : "content of current page does not exist";
            throw new NoElementException(message);
        }
    }

}
