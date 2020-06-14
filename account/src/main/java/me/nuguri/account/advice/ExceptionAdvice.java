package me.nuguri.account.advice;

import ch.qos.logback.core.joran.spi.NoAutoStartUtil;
import me.nuguri.common.dto.ErrorResponse;
import me.nuguri.common.exception.InvalidRequestException;
import me.nuguri.common.exception.NoAuthorityException;
import me.nuguri.common.exception.NoElementException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;

import static org.springframework.util.StringUtils.hasText;

@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalidRequest(InvalidRequestException e) {
        Errors errors = e.getErrors();
        String message = e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST, message, errors);
    }

    @ExceptionHandler(NoElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noElement(NoElementException e) {
        String message = e.getMessage();
        message = hasText(message) ? message : "no elements of page";
        return new ErrorResponse(HttpStatus.NOT_FOUND, message);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse entityNotFound(EntityNotFoundException e) {
        String message = e.getMessage();
        message = hasText(message) ? message : "not exist id of entity";
        return new ErrorResponse(HttpStatus.NOT_FOUND, message);
    }

    @ExceptionHandler(NoAuthorityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse noAuthority(NoAuthorityException e) {
        String message = e.getMessage();
        message = hasText(message) ? message : "has no authority can not access resource";
        return new ErrorResponse(HttpStatus.FORBIDDEN, message);
    }



}
