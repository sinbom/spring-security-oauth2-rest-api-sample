package me.nuguri.resc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "PTYPE", length = 1)
@Getter
@Setter
public class Product extends BaseEntity{

    @Column(nullable = false)
    private String name;

    @Column
    private Integer price;

    @Column(nullable = false)
    private int stockCount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Creator creator;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Company company;

    /** 카테고리  */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductCategory> productCategories = new ArrayList<>();

    /**
     * 양방향 관계 설정
     * @param creator 저작자
     */
    public void addCreator(Creator creator) {
        this.creator = creator;
        creator.getProducts().add(this);
    }

    /**
     * 양방향 관계 설정
     * @param company 회사
     */
    public void addCompany(Company company) {
        this.company = company;
        company.getProducts().add(this);
    }

}
