package me.nuguri.common.entity;

import lombok.*;
import me.nuguri.common.enums.ProductType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 상품 엔티티
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "PTYPE", length = 1)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public abstract class Product extends BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 자식 테이블 구분 컬럼 조회 필드 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, insertable = false, updatable = false, length = 1)
    private ProductType ptype;

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 가격 */
    @Column(nullable = false)
    private int price;

    /** 재고 수량 */
    @Column(nullable = false)
    private int stockCount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Creator creator;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Company company;

    /** 카테고리  */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductCategory> productCategories = new ArrayList<>();

    protected Product(Long id, String name, int price, int stockCount, Creator creator, Company company) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockCount = stockCount;
        this.addCreator(creator);
        this.addCompany(company);
    }

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
