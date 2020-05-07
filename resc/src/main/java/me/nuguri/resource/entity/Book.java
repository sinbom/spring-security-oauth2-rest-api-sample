package me.nuguri.resource.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Book {

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

    public void addCategoryBooks(CategoryBook categoryBook) {
        this.categoryBooks.add(categoryBook);
        categoryBook.setBook(this);
    }

}
