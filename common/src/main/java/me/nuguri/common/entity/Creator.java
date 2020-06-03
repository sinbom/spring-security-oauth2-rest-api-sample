package me.nuguri.common.entity;

import lombok.*;
import me.nuguri.common.enums.Gender;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 저자 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Creator extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 이름 */
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    /** 출생 날짜 */
    private LocalDate birth;

    /** 사망 날짜 */
    private LocalDate death;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    @Builder
    protected Creator(Long id, String name, Gender gender, LocalDate birth, LocalDate death) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.birth = birth;
        this.death = death;
    }
}
