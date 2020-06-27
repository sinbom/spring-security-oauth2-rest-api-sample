package me.nuguri.auth.repository;

import me.nuguri.common.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(attributePaths = "authority", type = EntityGraph.EntityGraphType.FETCH)
    @Transactional(readOnly = true)
    Optional<Account> findByEmail(String email);

}
