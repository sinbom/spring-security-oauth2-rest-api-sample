package me.nuguri.resc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Company extends BaseEntity {

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 설립 날짜 */
    @Column(nullable = false)
    private LocalDate establishDate;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

}
