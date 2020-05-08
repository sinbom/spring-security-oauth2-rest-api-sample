package me.nuguri.resource.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter @Setter
public class PaginationResource<T> extends EntityModel<T> {

    @Getter @Setter
    public static class PaginationInfo {
        private Integer size;
        private Long totalElements;
        private Integer totalPages;
        private Integer number;
        public <E> PaginationInfo(Page<E> number) {
            this.size = number.getSize();
            this.totalElements = number.getTotalElements();
            this.totalPages = number.getTotalPages();
            this.number = number.getNumber() + 1;
        }
    }

    private List<T> _contents;

    private PaginationInfo page;

    public <E> PaginationResource(Page<E> page, Function<? super E, ? extends T> function) {
        this._contents = page.stream().map(function).collect(Collectors.toList());
        this.page = new PaginationInfo(page);
    }

    public void addPaginationLink(Pagination pagination, WebMvcLinkBuilder builder) {
        String page = pagination.getPage();
        String size = pagination.getSize();
        String sort = pagination.getSort();
        int pageNum = this.page.number + 1;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (!StringUtils.isEmpty(size)) {
            map.add("size", size);
        }
        if (!StringUtils.isEmpty(sort)) {
            map.add("sort", sort);
        }
        String[] rels = {"first", "prev", "self", "next", "last"};
        String[] pages = {"1", pageNum > 1 ? pageNum - 1 + "" : "1", page,
                this.page.totalPages > pageNum ? pageNum + 1 + "" : pageNum + "", this.page.totalPages + ""};
        for (int i = 0; i < rels.length; i++) {
            if ("self".equals(rels[i]) && StringUtils.isEmpty(page)) {
                map.remove("page");
            } else {
                map.set("page", pages[i]);
            }
            add(new Link(builder.toUriComponentsBuilder().queryParams(map).toUriString()).withRel(rels[i]));
        }

    }

}
