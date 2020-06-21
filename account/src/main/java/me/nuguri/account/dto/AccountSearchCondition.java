package me.nuguri.account.dto;

import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.dto.PageableCondition;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.Roles;

@Getter
@Setter
public class AccountSearchCondition extends PageableCondition {

    /** 이메일 */
    private String email;

    /** 이름 */
    private String name;

    /** 성별 */
    private Gender gender;

    /** 시,도 */
    private String city;

    /** 도로명 주소 */
    private String street;

    /** 우편 번호 */
    private String zipCode;

    /** 권한 */
    private Roles roles;

}
