package com.fitness.service;

import com.fitness.model.Client;
import com.fitness.model.Membership;
import com.fitness.repository.ClientRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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

    public Page<Client> searchClients(String search, String membershipStatus, Pageable pageable) {
        // 1. Создаем спецификацию с учетом фильтров
        Specification<Client> spec = (root, query, cb) -> {
            query.distinct(true); // Убираем дубликаты
            List<Predicate> predicates = new ArrayList<>();

            // Фильтр по поиску
            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("phone")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }

            // Фильтр по статусу абонемента
            if (StringUtils.hasText(membershipStatus)) {
                Join<Client, Membership> membershipJoin = root.join("memberships", JoinType.LEFT);

                LocalDate today = LocalDate.now();
                switch (membershipStatus.toLowerCase()) {
                    case "active":
                        predicates.add(cb.and(
                                cb.isTrue(membershipJoin.get("isActive")),
                                cb.greaterThanOrEqualTo(membershipJoin.get("endDate"), today)
                        ));
                        break;
                    case "inactive":
                        predicates.add(cb.lessThan(membershipJoin.get("endDate"), today));
                        break;
                    case "expiring":
                        LocalDate weekLater = today.plusDays(7);
                        predicates.add(cb.between(membershipJoin.get("endDate"), today, weekLater));
                        break;
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Получаем страницу клиентов
        Page<Client> page = clientRepository.findAll(spec, pageable);

        // 3. Загружаем абонементы для полученных клиентов
        if (!page.getContent().isEmpty()) {
            List<Long> clientIds = page.getContent().stream()
                    .map(Client::getId)
                    .collect(Collectors.toList());

            List<Client> clientsWithMemberships = clientRepository.findAllWithMemberships(clientIds);

            // Сортируем абонементы по дате окончания
            clientsWithMemberships.forEach(client ->
                    client.getMemberships().sort(
                            Comparator.comparing(Membership::getEndDate).reversed()
                    ));

            return new PageImpl<>(clientsWithMemberships, pageable, page.getTotalElements());
        }

        return page;
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