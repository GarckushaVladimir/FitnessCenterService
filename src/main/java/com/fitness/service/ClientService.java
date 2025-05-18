package com.fitness.service;

import com.fitness.model.Client;
import com.fitness.model.Membership;
import com.fitness.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public List<Client> getAllClientsWithVisits() {
        List<Client> clients = clientRepository.findAll();
        // Инициализируем посещения для каждого клиента
        clients.forEach(client -> client.getVisits().size());
        return clients;
    }

    public List<Client> getClientsWithActiveMemberships() {
        return clientRepository.findAll().stream()
                .filter(client -> client.getMemberships().stream()
                        .anyMatch(Membership::isActive))
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveClient(Client client) {
        clientRepository.save(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }
}