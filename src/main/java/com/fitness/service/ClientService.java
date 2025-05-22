package com.fitness.service;

import com.fitness.model.Client;
import com.fitness.model.Membership;
import com.fitness.model.Visit;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final VisitService visitService;

    public ClientService(ClientRepository clientRepository, VisitService visitService) {
        this.clientRepository = clientRepository;
        this.visitService = visitService;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public Page<Client> getClientsWithVisits(Pageable pageable) {
        // Получаем страницу клиентов
        Page<Client> page = clientRepository.findAll(pageable);

        // Инициализируем посещения для каждого клиента
        List<Long> clientIds = page.getContent().stream()
                .map(Client::getId)
                .collect(Collectors.toList());

        List<Client> clientsWithVisits = clientRepository.findAllWithVisits(clientIds);

        return new PageImpl<>(clientsWithVisits, pageable, page.getTotalElements());
    }

    public List<Client> getAllClientsWithFilteredVisits(
            LocalDate startDate,
            LocalDate endDate,
            String programName,
            String trainerName,
            Integer minDuration,
            Integer maxDuration,
            String clientName) {

        List<Client> clients = clientRepository.findAll();

        boolean hasFilters = startDate != null || endDate != null
                || programName != null || trainerName != null
                || minDuration != null || maxDuration != null;

        clients.forEach(client -> {
            List<Visit> filteredVisits = visitService.searchVisits(
                            client.getId(),
                            startDate,
                            endDate,
                            programName,
                            trainerName,
                            minDuration,
                            maxDuration
                    ).stream()
                    .filter(v -> v.getClient().getId().equals(client.getId()))
                    .collect(Collectors.toList());

            // Фильтрация по имени клиента
            if (clientName != null && !client.getFullName().toLowerCase().contains(clientName.toLowerCase())) {
                filteredVisits = Collections.emptyList();
            }

            client.setVisits(filteredVisits);
        });

        // Если есть фильтры — оставляем только клиентов с посещениями
        if (hasFilters) {
            return clients.stream()
                    .filter(client -> !client.getVisits().isEmpty())
                    .collect(Collectors.toList());
        }

        // Без фильтров — возвращаем всех клиентов
        return clients;
    }

    public List<Client> getClientsWithActiveMemberships() {
        return clientRepository.findAll().stream()
                .filter(client -> client.getMemberships().stream()
                        .anyMatch(Membership::isActive))
                .collect(Collectors.toList());
    }

    public Page<Client> searchClients(String search, String membershipStatus, Pageable pageable) {
        Specification<Client> spec = buildSpecification(search, membershipStatus);
        Page<Client> page = clientRepository.findAll(spec, pageable);

        return postProcessPage(page);
    }

    private Specification<Client> buildSpecification(String search, String membershipStatus) {
        return (root, query, cb) -> {
//            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern), // Используется Java-поле fullName
                        cb.like(cb.lower(root.get("phone")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }

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

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Page<Client> postProcessPage(Page<Client> page) {
        if (!page.getContent().isEmpty()) {
            List<Long> clientIds = page.getContent().stream()
                    .map(Client::getId)
                    .collect(Collectors.toList());

            List<Client> clientsWithMemberships = clientRepository.findAllWithMemberships(clientIds);
            clientsWithMemberships.forEach(client ->
                    client.getMemberships().sort(
                            Comparator.comparing(Membership::getEndDate).reversed()
                    )
            );

            return new PageImpl<>(
                    clientsWithMemberships,
                    page.getPageable(),
                    page.getTotalElements()
            );
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