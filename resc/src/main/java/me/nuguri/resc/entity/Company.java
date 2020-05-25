package me.nuguri.resc.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Company extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 설립 날짜 */
    @Column(nullable = false)
    private LocalDate establishDate;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

}
