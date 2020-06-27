package me.nuguri.auth.repository.custom;

import me.nuguri.common.entity.Client;
import me.nuguri.common.entity.ClientResource;
import me.nuguri.common.entity.ClientScope;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface ClientRepositoryCustom {

    Optional<Client> findByClientIdFetchAuthority(String clientId);

    List<ClientScope> findClientScopesByIdFetchScopes(Long id);

    List<ClientResource> findClientResourcesByIdFetchResources(Long id);

}
