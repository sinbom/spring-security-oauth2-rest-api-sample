package me.nuguri.auth.repository;

import me.nuguri.auth.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {
}
