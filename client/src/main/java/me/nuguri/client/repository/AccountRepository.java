package me.nuguri.client.repository;

import me.nuguri.client.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Entity;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(attributePaths = "roles", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Account> findByEmail(String email);

}
