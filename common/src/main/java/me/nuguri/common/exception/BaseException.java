package me.nuguri.common.exception;

import lombok.Getter;
import org.springframework.validation.Errors;

@Getter
public abstract class BaseException extends RuntimeException {

    /**
     * 에러 정보 객체
     */
    private final Errors errors;

    public BaseException(Errors errors, String message) {
        super(message);
        this.errors = errors;
    }

    public BaseException(String message) {
        super(message);
        this.errors = null;
    }
}
