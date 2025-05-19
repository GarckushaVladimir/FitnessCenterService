package com.fitness.service;

import com.fitness.model.Client;
import com.fitness.model.Membership;
import com.fitness.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public Page<Client> searchClients(String search, Pageable pageable) {
        Specification<Client> spec = (root, query, cb) -> {
            if (search == null || search.isEmpty()) return null;
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("phone")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
            );
        };
        return clientRepository.findAll(spec, pageable);
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