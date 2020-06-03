import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.common.dto.BaseSearchCondition;
import me.nuguri.common.dto.ErrorResponse;
import me.nuguri.common.dto.PageableCondition;
import me.nuguri.common.support.PaginationValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.awt.print.Pageable;

public class PaginationValidatorTest {

    private final ObjectMapper objectMapper= new ObjectMapper();

    private final Jackson2JsonParser parser = new Jackson2JsonParser();

    @ParameterizedTest(name = "{index}. {displayName} parameter(sort: {arguments})")
    @DisplayName("유저 정보 리스트 조건 값 조회 성공적으로 얻는 경우")
    @CsvSource(
            value = {"1:10:"},
            delimiter = ':'
    )
    public void test() {
        // given
        PageableCondition condition = new PageableCondition();
        condition.setPage("1");
        condition.setSize("");
        PaginationValidator paginationValidator = new PaginationValidator();
        Errors testErrors = new BeanPropertyBindingResult(condition, "condition");
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "error", testErrors);
        // when
        paginationValidator.validate(condition, PageableCondition.class, testErrors);


    }

}
