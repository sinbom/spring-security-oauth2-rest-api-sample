import me.nuguri.common.dto.PageableCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class PageableConditionTest {

    @ParameterizedTest(name = "{index}. {displayName} parameter(sort: {arguments})")
    @DisplayName("페이지 조건 값으로 페이징 객체 생성 성공적인 경우")
    @CsvSource(value = {"1:20:startCreated,asc", "2:10:endCreated,desc", "10:5:startUpdated"}, delimiter = ':')
    public void success(String page, String size, String sort) {
        // given
        String[] sorts = sort.split(",");
        PageableCondition condition = new PageableCondition();
        condition.setPage(page);
        condition.setSize(size);
        condition.setSort(sort);
        // when
        Pageable pageable = condition.getPageable();
        // then
        Iterator<Sort.Order> orders = pageable.getSort().iterator();
        assertEquals(pageable.getPageNumber() + 1 + "", page);
        assertEquals(pageable.getPageSize() + "", size);
        for (int i = 0; orders.hasNext(); i++) {
            Sort.Order order = orders.next();
            String direction = sorts.length - 1 == 0 ? "asc" : sorts[sorts.length - 1];
            assertEquals(order.getProperty(), sorts[i]);
            assertTrue(order.getDirection().toString().equalsIgnoreCase(direction));
        }
    }

    @ParameterizedTest(name = "{index}. {displayName} parameter(sort: {arguments})")
    @DisplayName("페이지 조건 값으로 페이징 객체 생성 실패하는 경우")
    @CsvSource(value = {"a:b:zxczxc,asc", "1:zxvc:endCreated,zxc", "1:10:zxc,mnb"}, delimiter = ':')
    public void error(String page, String size, String sort) {
        // given
        String[] sorts = sort.split(",");
        PageableCondition condition = new PageableCondition();
        condition.setPage(page);
        condition.setSize(size);
        condition.setSort(sort);
        // when, then
        assertThrows(IllegalArgumentException.class, condition::getPageable);
    }

}
