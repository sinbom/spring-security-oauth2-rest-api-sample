package me.nuguri.common.support;

import org.springframework.validation.Errors;

import java.util.List;

public class BaseValidator {

    /**
     * 식별키가 0이상의 정수인지 검증
     *
     * @param ids    식별키
     * @param errors 에러
     */
    public void validate(List<Long> ids, Errors errors) {
        boolean isAnyLowerThanZero = ids.stream().anyMatch(id -> id < 1);
        if (isAnyLowerThanZero) {
            errors.rejectValue("ids", "wrongValue", "id is must be greater than zero");
        }
    }

}
