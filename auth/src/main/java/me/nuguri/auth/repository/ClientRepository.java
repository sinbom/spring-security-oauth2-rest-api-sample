package me.nuguri.auth.repository;

import me.nuguri.auth.repository.custom.ClientRepositoryCustom;
import me.nuguri.common.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long>, ClientRepositoryCustom {
}