package me.nuguri.resource.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class MinorCategory {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private MajorCategory majorCategory;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<CategoryBook> categoryBooks = new ArrayList<>();

    public void addMajorCategory(MajorCategory category) {
        this.majorCategory = category;
        category.getMinorCategories().add(this);
    }

    public void addCategoryBook(CategoryBook categoryBook) {
        this.categoryBooks.add(categoryBook);
        categoryBook.setCategory(this);
    }

}
