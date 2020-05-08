package me.nuguri.auth.validator;

import me.nuguri.auth.domain.Pagination;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import java.util.stream.Stream;

@Component
public class PaginationValidator {

    /**
     * Pagination 도메인 condition 값 중 페이지, 페이지 사이즈, 정렬 방식 검증
     * @param pagination page 페이지 번호, size 페이지 사이즈, sort 정렬 방식
     * @param entityType 페이징 객체 엔티티 타입 클래스
     * @param errors 에러
     * @param <T> 페이징 객체 엔티티 타입
     */
    public <T> void validate(Pagination pagination, Class<T> entityType, Errors errors) {
        if (!StringUtils.isEmpty(pagination.getPage())) {
            if (!pagination.getPage().matches("^[1-9][0-9]*$")) {
                errors.rejectValue("page", "wrongValue", "page is wrong");
            }
        }
        if (!StringUtils.isEmpty(pagination.getSize())) {
            if (!pagination.getSize().matches("^[1-9][0-9]*$")) {
                errors.rejectValue("size", "wrongValue", "size is wrong");
            }
        }
        if (!StringUtils.isEmpty(pagination.getSort())) {
            String[] sort = pagination.getSort().split(",");
            if (sort.length > 1) {
                for (int i = 0; i < sort.length - 1; i++) {
                    String property = sort[i];
                    if (Stream.of(entityType.getDeclaredFields()).noneMatch(f -> f.getName().equals(property))) {
                        errors.rejectValue("sort", "wrongValue", "sort property is wrong");
                    }
                }
                if (!sort[sort.length - 1].equalsIgnoreCase("asc") && !sort[sort.length - 1].equalsIgnoreCase("desc")) {
                    errors.rejectValue("sort", "wrongValue", "sort direction is wrong");
                }
            } else if (sort.length == 1){
                if (Stream.of(entityType.getDeclaredFields()).noneMatch(f -> f.getName().equals(sort[0]))) {
                    errors.rejectValue("sort", "wrongValue", "sort property is wrong");
                }
            }
        }
    }

}
