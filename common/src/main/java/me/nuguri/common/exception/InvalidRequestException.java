package me.nuguri.common.exception;

import lombok.NoArgsConstructor;
import org.springframework.validation.Errors;

@NoArgsConstructor
public class InvalidRequestException extends BaseException {

    public InvalidRequestException(Errors errors, String message) {
        super(errors, message);
    }

}
