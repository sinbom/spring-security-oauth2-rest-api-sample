package me.nuguri.auth.repository;

import me.nuguri.common.entity.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @EntityGraph(
            attributePaths = {
                    "clientAuthorities",
                    "clientScopes",
                    "clientResources",
                    "clientGrantTypes",
                    "clientRedirectUris"
            },
            type = EntityGraph.EntityGraphType.FETCH
    )
    @Transactional(readOnly = true)
    Optional<Client> findByClientId(String clientId);

}
