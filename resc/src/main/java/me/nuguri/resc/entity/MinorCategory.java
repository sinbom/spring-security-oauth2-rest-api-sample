package me.nuguri.resc.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class MinorCategory extends BaseEntity {

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

}
