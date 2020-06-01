package me.nuguri.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public abstract class BaseSearchCondition {

    /** 등록 날짜 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate created;

    /** 수정 날짜 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate updated;

    public abstract Map<String, String> paramsToMap();

}
