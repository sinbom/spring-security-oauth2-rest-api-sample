package me.nuguri.account.repository;

import me.nuguri.common.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom {

    @Override
    Page<Account> findAll(Pageable pageable);

    @Override
    Optional<Account> findById(Long id);

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);


}
