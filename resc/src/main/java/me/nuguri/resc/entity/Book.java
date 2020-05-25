package me.nuguri.resc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 책 엔티티
 */
@Entity
@DiscriminatorValue("B")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
public class Book extends Product {

    /** 출판 날짜 */
    @Column(nullable = false)
    private LocalDate publishDate;

}
