package me.nuguri.resource.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class CategoryBook extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private MinorCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    public void addMinorCategory(MinorCategory category) {
        this.category = category;
        category.getCategoryBooks().add(this);
    }

    public void addBook(Book book) {
        this.book = book;
        book.getCategoryBooks().add(this);
    }

}
