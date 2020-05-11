package me.nuguri.resc.entity;

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
@EqualsAndHashCode(of = "id", callSuper = false)
public class MajorCategory extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "majorCategory", cascade = CascadeType.ALL)
    private List<MinorCategory> minorCategories = new ArrayList<>();

    public void addMinorCategory(MinorCategory category) {
        this.minorCategories.add(category);
        category.setMajorCategory(this);
    }

}
