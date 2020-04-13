package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.repository.ClientRepository;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService implements ClientDetailsService {

    private final ClientRepository clientRepository;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = clientRepository.findByClientId(clientId).orElseThrow(() -> new ClientRegistrationException(clientId));
        BaseClientDetails clientDetails = new BaseClientDetails();
        return clientDetails;
    }
}
