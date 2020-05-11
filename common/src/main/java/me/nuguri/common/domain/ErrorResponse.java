package me.nuguri.common.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import me.nuguri.common.serializer.ErrorsSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private LocalDateTime timestamp = LocalDateTime.now();

    private int status;

    private String error;

    private String message;

    @JsonSerialize(using = ErrorsSerializer.class)
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
