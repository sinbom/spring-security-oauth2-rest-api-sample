package me.nuguri.auth.service.lazy;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.repository.ClientRepository;
import me.nuguri.common.entity.Client;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorizationLazyService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Client findByClientIdFetchAndLazy(String clientId) {
        Client client = clientRepository
                .findByClientIdFetchAuthority(clientId)
                .orElseThrow(() -> new ClientRegistrationException(clientId));
        // lazy loading 또는 이미 조회 되어 persistence context 관리 중인 엔티티 검색
        client
                .getClientRedirectUris()
                .size(); // lazy loading
        client
                .getClientGrantTypes()
                .size(); // lazy loading
        client
                .getClientScopes()
                .forEach(cs -> cs.getScope().getName()); // find at persistence context
        client
                .getClientResources()
                .forEach(cr -> cr.getResource().getName()); // find at persistence context
        return client;
    }

}
