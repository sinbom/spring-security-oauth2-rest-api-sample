package me.nuguri.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Getter
@Setter
public class PageableCondition extends BaseSearchCondition {

    /** 페이지 */
    private String page;

    /** 사이즈 */
    private String size;

     /** 정렬 */
    private String sort;

    public Pageable getPageable() {
        int page = StringUtils.isEmpty(this.page) ? 0 : Integer.parseInt(this.page) - 1;
        int size = StringUtils.isEmpty(this.size) ? 10 : Integer.parseInt(this.size);
        if (StringUtils.isEmpty(sort)) {
            return PageRequest.of(page, size);
        } else {
            String[] sorts = sort.split(",");
            if (sorts.length == 1) {
                return PageRequest.of(page, size, Sort.by(sorts[0]));
            } else {
                String direction = sorts[sorts.length - 1];
                Sort.Direction sort = Sort.Direction.fromString(direction);
                String[] properties = Arrays.copyOfRange(sorts, 0, sorts.length - 1);
                return PageRequest.of(page, size, sort, properties);
            }
        }
    }
}
