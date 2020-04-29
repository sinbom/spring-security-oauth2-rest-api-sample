package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    public Client find(String clientId) {
        return clientRepository.findById(clientId).orElseThrow(RuntimeException::new);
    }

    public Client generate(Client client) {
        return clientRepository.save(client);
    }

    public void delete(String clientId) {
        clientRepository.deleteById(clientId);
    }


}
