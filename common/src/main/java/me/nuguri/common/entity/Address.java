package me.nuguri.common.entity;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 주소 컬럼, 임베디드 객체
 */
@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"city", "street", "zipCode"})
public class Address implements Serializable {

    private static final Long serialVersionUID = 1L;

    /** 시,도 */
    private String city;

    /* 상세 주소 */
    private String street;

    /** 우편 번호 */
    private String zipCode;

}
