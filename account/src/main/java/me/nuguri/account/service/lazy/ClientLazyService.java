package me.nuguri.account.service.lazy;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.service.ClientService;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientLazyService {

    private final ClientService clientService;

    /**
     * 클라이언트 엔티티 수정 전 유저가 보유한 클라이언트 프록시 컬렉션을 지연 로딩 후, 리소스 권한을 검사한 후 수정 한다.
     * @param client 클라이언트
     * @param account 유저
     * @return 수정한 클라이언트 엔티티
     */
    public Client update(Client client, Account account) {
        List<Client> clients = account.getClients();
        clients.size(); // Lazy Loading
        checkAuthority(account, client);
        return clientService.update(client);
    }

    private void checkAuthority(Account user, Client client) {
        Role role = user.getRole(); // 토큰 발급 유저 권한
        List<Client> clients = user.getClients(); // 유저가 보유한 리소스
        if (!role.equals(Role.ADMIN) ||
                clients.stream().noneMatch(c -> c.equals(client))) { // 현재 토큰 유저가 관리자 권한이거나 리소스 소유자인 경우
            throw new AccessDeniedException("can not access resource because has no authority");
        }
    }

}
