package me.nuguri.resc.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 상위 카테고리 엔티티
 */
@Entity
@Getter
@Setter
public class MajorCategory extends BaseEntity {

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 하위 카테고리 */
    @OneToMany(mappedBy = "majorCategory", cascade = CascadeType.ALL)
    private List<MinorCategory> minorCategories = new ArrayList<>();

    public String getName() {
        return name;
    }
}
