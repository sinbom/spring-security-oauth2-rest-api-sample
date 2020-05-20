package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.repository.ClientRepository;
import me.nuguri.common.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
