import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.common.dto.BaseSearchCondition;
import me.nuguri.common.dto.ErrorResponse;
import me.nuguri.common.dto.PageableCondition;
import me.nuguri.common.support.PaginationValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PaginationValidatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Jackson2JsonParser parser = new Jackson2JsonParser();

    @ParameterizedTest(name = "{index}. {displayName} parameter(sort: {arguments})")
    @DisplayName("페이지 조건 값 검증 성공적인 경우")
    @CsvSource(value = {"1:20:startCreated,asc", "2:10:endCreated,desc", "10:5:startUpdated"}, delimiter = ':')
    public void success(String page, String size, String sort) throws JsonProcessingException {
        // given
        PageableCondition pageableCondition = new PageableCondition();
        pageableCondition.setPage(page);
        pageableCondition.setSize(size);
        pageableCondition.setSort(sort);
        PaginationValidator paginationValidator = new PaginationValidator();
        Errors testErrors = new BeanPropertyBindingResult(pageableCondition, "condition");
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "error", testErrors);
        // when
        paginationValidator.validate(pageableCondition, BaseSearchCondition.class, testErrors);
        assertThat(errorResponse).hasNoNullFieldsOrProperties();
        String json = objectMapper.writeValueAsString(errorResponse);
        List<Map<String, String>> errors = (List<Map<String, String>>) parser.parseMap(json).get("errors");
        // then
        errors.forEach(error -> assertThat(error).isEmpty());
    }

    @ParameterizedTest(name = "{index}. {displayName} parameter(sort: {arguments})")
    @DisplayName("페이지 조건 값 검증 실패하는 경우")
    @CsvSource(value = {"1-0:0:-98", "asd:08:-12", "0:-2:id,qwe"}, delimiter = ':')
    public void fail(String page, String size, String sort) throws JsonProcessingException {
        // given
        PageableCondition pageableCondition = new PageableCondition();
        pageableCondition.setPage(page);
        pageableCondition.setSize(size);
        pageableCondition.setSort(sort);
        PaginationValidator paginationValidator = new PaginationValidator();
        Errors testErrors = new BeanPropertyBindingResult(pageableCondition, "condition");
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "error", testErrors);
        // when
        paginationValidator.validate(pageableCondition, BaseSearchCondition.class, testErrors);
        String json = objectMapper.writeValueAsString(errorResponse);
        List<Map<String, String>> errors = (List<Map<String, String>>) parser.parseMap(json).get("errors");
        // then
        errors.forEach(error -> assertThat(error).hasNoNullFieldsOrProperties());
    }


}
