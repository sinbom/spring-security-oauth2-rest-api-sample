import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.common.dto.BaseResponse;
import me.nuguri.common.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorsSerializerTest {

    private final ObjectMapper objectMapper= new ObjectMapper();

    private final Jackson2JsonParser parser = new Jackson2JsonParser();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Test
    @DisplayName("에러 정보 직렬화 성공적인 경우")
    public void test() throws IOException {
        // given
        LocalDateTime created = LocalDateTime.now();
        LocalDateTime updated = LocalDateTime.now().minusDays(1L);
        String[] rejectedValue = {
                formatter.format(created),
                formatter.format(updated)
        };
        String objectName = "testObject";
        String[] fields = {
                "created",
                "updated"
        };
        String[] codes = {
                "wrongCreatedValue",
                "wrongUpdatedValue",
                "wrongDateTime"
        };
        String[] defaultMessages = {
                "created is must be before than updated",
                "updated is must be after than created",
                "created or updated is wrong"
        };
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCreated(created);
        baseResponse.setUpdated(updated);
        Errors testErrors = new BeanPropertyBindingResult(baseResponse, objectName);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "error", testErrors);
        // when
        if (created.isAfter(updated)) {
            testErrors.rejectValue(fields[0], codes[0], defaultMessages[0]); // value error
            testErrors.rejectValue(fields[1], codes[1], defaultMessages[1]); // value error
            testErrors.reject(codes[2], defaultMessages[2]); // global error
        }
        // then
        String json = objectMapper.writeValueAsString(errorResponse);
        Map<String, Object> jsonToMap = parser.parseMap(json);
        List<Map<String, String>> errors = (List<Map<String, String>>) jsonToMap.get("errors");
        for (int i = 0; i < errors.size(); i++) {
            Map<String, String> error  = errors.get(i);
            if (i != errors.size() - 1) {
                assertEquals(error.get("field"), fields[i]);
                assertEquals(error.get("rejectedValue"), rejectedValue[i]);
            }
            assertEquals(error.get("objectName"), objectName);
            assertEquals(error.get("defaultMessage"), defaultMessages[i]);
            assertEquals(error.get("code"), codes[i]);
        }
    }

}
