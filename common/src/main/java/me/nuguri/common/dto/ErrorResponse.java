package me.nuguri.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.support.ErrorsSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponse {

    /** 응답 시간 */
    private LocalDateTime timestamp = LocalDateTime.now();

    /** 상태 값 */
    private int status;

    /** 에러 코드 */
    private String error;

    /** 에러 메시지 */
    private String message;

    /** 에러 상세 정보 */
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
