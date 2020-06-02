package me.nuguri.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BaseResponse {

    /** 생성 시간 */
    private LocalDateTime created;

    /** 최종 수정 시간 */
    private LocalDateTime updated;

}
