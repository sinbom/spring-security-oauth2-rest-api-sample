package me.nuguri.auth.domain;

import lombok.Data;
import org.apache.coyote.ErrorState;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Data
public class ErrorResponse {

    private LocalDateTime timeStamp = LocalDateTime.now();

    private int status;

    private String error;

    private String message;

    private Errors errors;

    public ErrorResponse(HttpStatus httpStatus, String message) {
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.message = message;
    }

    public ErrorResponse(HttpStatus httpStatus, String message, Errors errors) {
        this(httpStatus, message);
        this.errors = errors;
    }
}
