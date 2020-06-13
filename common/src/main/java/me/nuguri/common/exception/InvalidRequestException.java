package me.nuguri.common.exception;

import org.springframework.validation.Errors;

public class InvalidRequestException extends BaseException {

    public InvalidRequestException(Errors errors, String message) {
        super(errors, message);
    }

}
