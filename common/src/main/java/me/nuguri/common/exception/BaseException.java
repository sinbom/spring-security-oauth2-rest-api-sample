package me.nuguri.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;

@Getter
@NoArgsConstructor
public abstract class BaseException extends RuntimeException {

    /**
     * 에러 정보 객체
     */
    private Errors errors;

    public BaseException(Errors errors, String message) {
        super(message);
        this.errors = errors;
    }

    public BaseException(String message) {
        super(message);
        this.errors = null;
    }

}
