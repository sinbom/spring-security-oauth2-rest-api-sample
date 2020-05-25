package me.nuguri.resc.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * 공통 엔티티
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public abstract class BaseEntity {

    /** 식별키 */
    @Id
    @GeneratedValue
    private Long id;

    /** 생성 날짜 */
    @CreatedDate
    private LocalDateTime created;

    /** 수정 날짜 */
    @LastModifiedDate
    private LocalDateTime updated;

}
