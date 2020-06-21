package me.nuguri.account.dto;

import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.dto.PageableCondition;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Roles;
import me.nuguri.common.enums.Scopes;

@Getter
@Setter
public class ClientSearchCondition extends PageableCondition {

    /** 클라이언트 Id */
    private String clientId;

    /** 리소스 Id */
    private String resourceId;

    /** 접근 범위 */
    private Scopes scopes;

    /** 권한 부여 방식 */
    private GrantType grantType;

    /** 리다이렉트 URI */
    private String redirectUri;

    /** 권한 */
    private Roles authority;

    /** 클라이언트 등록 계정 */
    private String email;

}
