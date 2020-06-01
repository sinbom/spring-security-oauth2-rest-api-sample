package me.nuguri.account.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.repository.ClientRepository;
import me.nuguri.common.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * 클아이언트 엔티티 페이지 조회
     * @param pageable 페이징
     * @return 조회한 클라이언트 엔티티 페이징 객체
     */
    @Transactional(readOnly = true)
    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    /**
     * 클라이언트  엔티티 조회, 대리키(clientId) 조회
     * @param clientId 클라이언트 id
     * @return 조회한 클라이언트 엔티티 객체
     */
    @Transactional(readOnly = true)
    public Client find(String clientId) {
        return clientRepository.findById(clientId).orElseThrow(RuntimeException::new);
    }

    /**
     * 클라이언트 엔티티 생성, 입력 받은 파라미터 값으로 생성
     * @param client resourceIds 접근 리소스, redirectUri 리다이렉트 uri
     * @return 생성한 클라이언트 엔티티 객체
     */
    public Client generate(Client client) {
        client.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        return clientRepository.save(client);
    }

}
