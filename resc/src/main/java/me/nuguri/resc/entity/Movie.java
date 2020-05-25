package me.nuguri.resc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("M")
@Getter
@Setter
public class Movie extends Product {

    /** 개봉 날짜 */
    @Column(nullable = false)
    private LocalDate releaseDate;

}
