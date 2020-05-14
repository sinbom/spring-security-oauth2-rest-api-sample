package me.nuguri.resc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<CategoryBook> categoryBooks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Author author;

    private LocalDate pubDate;

    public void addAuthor(Author author) {
        this.author = author;
        author.getBooks().add(this);
    }

}
