package me.nuguri.account.repository;

import me.nuguri.common.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, String>, ClientRepositoryCustom {
}
