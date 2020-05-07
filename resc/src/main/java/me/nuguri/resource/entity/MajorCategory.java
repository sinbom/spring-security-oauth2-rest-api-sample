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
public class MajorCategory {

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
