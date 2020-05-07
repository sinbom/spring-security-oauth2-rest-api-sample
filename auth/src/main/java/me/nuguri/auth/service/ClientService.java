package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.enums.GrantType;
import me.nuguri.auth.enums.Role;
import me.nuguri.auth.enums.Scope;
import me.nuguri.auth.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Client find(String clientId) {
        return clientRepository.findById(clientId).orElseThrow(RuntimeException::new);
    }

    public Client generate(Client client) {
        client.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        return clientRepository.save(client);
    }

}
