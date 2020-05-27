package me.nuguri.resc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import java.time.LocalDate;

/**
 * 책 엔티티
 */
@Entity
@DiscriminatorValue("B")
@PrimaryKeyJoinColumn(name = "book_id")
@Getter
@Setter
public class Book extends Product {

    /** 출판 날짜 */
    @Column(nullable = false)
    private LocalDate publishDate;

}
