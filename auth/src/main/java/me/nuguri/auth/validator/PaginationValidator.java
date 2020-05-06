package me.nuguri.auth.validator;

import me.nuguri.auth.domain.Pagination;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import java.util.stream.Stream;

@Component
public class PaginationValidator {

    public <T> void validate(Pagination pagination, Class<T> entityType, Errors errors) {
        if (!pagination.getPage().matches("^[1-9][0-9]*$")) {
            errors.rejectValue("page", "wrongValue", "page is wrong");
        }
        if (!pagination.getSize().matches("^[1-9][0-9]*$")) {
            errors.rejectValue("size", "wrongValue", "size is wrong");
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
