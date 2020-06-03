package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
    protected Company(Long id, String name, LocalDate establishDate) {
        this.id = id;
        this.name = name;
        this.establishDate = establishDate;
    }
}
