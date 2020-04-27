package me.nuguri.resource.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class Pagination {

    private int page = 1;

    private int pageSize = 10;

    public Pageable getPageable() {
        return PageRequest.of(this.getPage() - 1, this.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
    }

}
