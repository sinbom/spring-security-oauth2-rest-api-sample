package me.nuguri.auth.domain;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

@Data
public class Pagination {

    private String page;

    private String size;

    private String sort;

    public Pageable getPageable() {
        String[] sorts = sort.split(",");
        if (sorts.length == 0) {
            return PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));
        } else if (sorts.length == 1) {
            return PageRequest.of(Integer.parseInt(page), Integer.parseInt(size), Sort.by(sorts[0]));
        } else {
            String direction = sorts[sorts.length - 1];
            String[] properties = Arrays.copyOfRange(sorts, 0, sorts.length - 1);
            return PageRequest.of(Integer.parseInt(page), Integer.parseInt(size), Sort.Direction.fromString(direction), properties);
        }
    }
}
