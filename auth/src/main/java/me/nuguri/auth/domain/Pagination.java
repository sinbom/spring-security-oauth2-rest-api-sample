package me.nuguri.auth.domain;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Data
public class Pagination {

    private String page;

    private String size;

    private String sort;

    public Pageable getPageable() {
        String page = StringUtils.isEmpty(this.page) ? "1" : this.page;
        String size = StringUtils.isEmpty(this.size) ? "10" : this.size;
        if (StringUtils.isEmpty(sort)) {
            return PageRequest.of(Integer.parseInt(page) - 1, Integer.parseInt(size));
        } else {
            String[] sorts = sort.split(",");
            if (sorts.length == 1) {
                return PageRequest.of(Integer.parseInt(page) - 1, Integer.parseInt(size), Sort.by(sorts[0]));
            } else {
                String direction = sorts[sorts.length - 1];
                String[] properties = Arrays.copyOfRange(sorts, 0, sorts.length - 1);
                return PageRequest.of(Integer.parseInt(page) - 1, Integer.parseInt(size), Sort.Direction.fromString(direction), properties);
            }
        }
    }
}
