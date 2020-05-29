package me.nuguri.auth.controller.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.auth.annotation.Oauth2AccessToken;
import me.nuguri.auth.annotation.Oauth2Authentication;
import me.nuguri.common.domain.AccountAdapter;
import me.nuguri.common.entity.Account;
import me.nuguri.common.enums.Role;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class AuthorizationApiController {

    private final TokenStore tokenStore;

    private final ModelMapper modelMapper;

//  jwt 방식의 토큰 (header.payload.signature)은 서버에서 별도로 저장하지 않아 stateless하며 토큰 자체에서 정보를 가진다
//  header.payload의 값을 base64 디코딩한 값과 signature 값을 헤더에 존재하는 해싱 알고리즘과 개인키(비밀키)로
//  디코딩한 값인 header.payload가 일치하고(일치하다는 것은 누군가 임의로 변경하지 않았다는 뜻, 변경하려면 개인키로 해싱해야되는데 개인키를 모르면 못함)
//  토큰내 정보인 만료 시간을 지나지 않았으면 그 자체로 유효하기 때문에 시간이 지나서 만료되는 경우가 아니라면 토큰을 만료시킬 수 없다
//  토큰의 존재 자체와 header.payload = signature의 일치와 만료 시간이 넘지 않은 것 자체가 유효한 것이기 때문

//    /**
//     * Authorization Header 통해 전달 받은 Bearer 토큰이 유효하면 만료시킨다.
//     * @param accessToken 엑세스 토큰
//     * @return
//     */
//    @PostMapping(value = "/oauth/revoke_token", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> revokeToken(@Oauth2AccessToken DefaultOAuth2AccessToken accessToken) {
//        tokenStore.removeAccessToken(accessToken);
//        return ResponseEntity.ok(accessToken);
//    }

    /**
     * Authorization Header 통해 전달 받은 Bearer 토큰으로 토큰을 발급 받은 계정 정보 조회
     * @param authentication 인증 객체
     * @return
     */
    @GetMapping(value = "/oauth/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMe(@Oauth2Authentication OAuth2Authentication authentication) {
        Account account = ((AccountAdapter) authentication.getPrincipal()).getAccount();
        return ResponseEntity.ok(modelMapper.map(account, GetMeResponse.class));
    }

    // ==========================================================================================================================================
    // Domain
    @Getter @Setter
    public static class GetMeResponse {
        private Long id;
        private String email;
        private String name;
        private Set<Role> roles;
    }
    // ==========================================================================================================================================
}
