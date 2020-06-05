package me.nuguri.account.dto;

import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.dto.PageableCondition;
import me.nuguri.common.enums.Scope;

@Getter
@Setter
public class ClientSearchCondition extends PageableCondition {

    /** 클라이언트 Id */
    private String clientId;

    /** 리소스 Id */
    private String resourceIds;

    /** 접근 범위 */
    private Scope scope;

    /** 권한 부여 방식 */
    private String grantTypes;

    /** 리다이렉트 URI */
    private String redirectUri;

    /** 권한 */
    private String authorities;

    /** 클라이언트 등록 계정 */
    private String email;

}
