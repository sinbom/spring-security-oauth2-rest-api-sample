package me.nuguri.resc.domain;

import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.enums.Gender;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CreatorSearchCondition {

    /** 이름 */
    private String name;

    /** 성별 */
    private Gender gender;

    /** 출생 날짜 from */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startBirth;

    /** 사망 날짜 from */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDeath;

    /** 출생 날짜 to */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endBirth;

    /** 사망 날짜 to */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDeath;

    public Map<String, String> paramsToMap() {
        Map<String, String> params = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (StringUtils.hasText(name)) {
            params.put("name", name);
        }
        if (gender != null) {
            params.put("gender", gender.toString());
        }
        if (startBirth != null) {
            params.put("startBirth", formatter.format(startBirth));
        }
        if (endBirth != null) {
            params.put("endBirth", formatter.format(endBirth));
        }
        if (startDeath != null) {
            params.put("startDeath", formatter.format(startDeath));
        }
        if (endDeath != null) {
            params.put("endDeath", formatter.format(endDeath));
        }
        return params;
    }

}
