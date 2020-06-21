package me.nuguri.common.entity;

import lombok.*;

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
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends Product {

    /** 출판 날짜 */
    @Column(nullable = false)
    private LocalDate publishDate;

    @Builder
    protected Book(Long id, LocalDate publishDate, String name, int price, int stockCount, Creator creator, Company company) {
        super(id, name, price, stockCount, creator, company);
        this.publishDate = publishDate;
    }

}
