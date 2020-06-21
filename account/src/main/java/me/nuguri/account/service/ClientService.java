package me.nuguri.account.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.repository.ClientRepository;
import me.nuguri.common.adapter.AuthenticationAdapter;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.Roles;
import me.nuguri.common.exception.NoAuthorityException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    public Client findById(Long id, AuthenticationAdapter authentication) {
        Long ownerId = authentication.getId();
        List<Roles> authorities = authentication.getAuthorities();
        Client client = clientRepository
                .findById(id)
                .orElseThrow(EntityNotFoundException::new);
        if (authorities.stream().noneMatch(r -> r.equals(Roles.ADMIN)) && !client.getAccount().getId().equals(ownerId)) {
            throw new NoAuthorityException();
        }
        return clientRepository
                .findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 클라이언트 엔티티 생성, 입력 받은 파라미터 값으로 생성
     *
     * @param client resourceIds 접근 리소스, redirectUri 리다이렉트 uri
     * @return 생성한 클라이언트 엔티티 객체
     */
    public Client generate(Client client) {
        String clientSecret = client.getClientSecret();
        clientSecret = passwordEncoder.encode(clientSecret);
        client.setClientSecret(clientSecret);
        return clientRepository.save(client);
    }

    /**
     * 클라이언트 엔티티 수정, 입력 받은 파라미터(Not Null Fields)만 대입해서 수정
     *
     * @param client resourceIds 접근 리소스, redirectUri 리다이렉트 uri
     * @return 수정한 클라이언트 엔티티 객체
     */
    public Client update(Client client, AuthenticationAdapter authentication) {
        Long id = client.getId();
        Client update = findById(id, authentication);
        String redirectUri = client.getRedirectUri();
        String resourceIds = client.getResourceIds();
        if (hasText(redirectUri)) {
            update.setRedirectUri(redirectUri);
        }
        if (hasText(resourceIds)) {
            update.setResourceIds(resourceIds);
        }
        return update;
    }

    /**
     * 클라이언트 엔티티 병합, 입력 받은 모든 파라미터 모두 대입해서 수정, 식별키에 해당하는 유저가 없는 경우 생성하지는 않음
     *
     * @param client resourceIds 접근 리소스, redirectUri 리다이렉트 uri
     * @return 병합한 클라이언트 엔티티 객체
     */
    public Client merge(Client client, AuthenticationAdapter authentication) {
        Long id = client.getId();
        Client merge = findById(id, authentication);
        String redirectUri = client.getRedirectUri();
        String resourceIds = client.getResourceIds();
        merge.setRedirectUri(redirectUri);
        merge.setResourceIds(resourceIds);
        return merge;
    }

    /**
     * 클라이언트 엔티티 삭제
     *
     * @param id             식별키
     * @param authentication 토큰 정보
     */
    public void delete(Long id, AuthenticationAdapter authentication) {
        Client client = findById(id, authentication);
        clientRepository.delete(client);
    }
}
