package me.nuguri.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public abstract class BaseSearchCondition {

    /** 등록 날짜 검색 시작 날짜*/
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startCreated;

    /** 등록 날짜 검색 종료 날짜 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endCreated;

    /** 수정 날짜 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startUpdated;

    /** 수정 날짜 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endUpdated;

}
