package com.fitness.service;

import com.fitness.model.Client;
import com.fitness.model.Membership;
import com.fitness.model.Visit;
import com.fitness.repository.VisitRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
//@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;

    public VisitService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public List<Visit> getVisitsByClientId(Long clientId) {
        return visitRepository.findByClientIdOrderByVisitDateDesc(clientId);
    }

    public Visit getVisitById(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found"));
    }

    @Transactional
    public Visit saveVisit(Visit visit) {
        Client client = visit.getClient();
        LocalDateTime visitDate = visit.getVisitDate();

        // 1. Проверяем, был ли у клиента активный абонемент НА ДАТУ ПОСЕЩЕНИЯ
        boolean hasValidMembership = client.getMemberships().stream()
                .anyMatch(m ->
                        !visitDate.toLocalDate().isBefore(m.getStartDate()) &&
                                !visitDate.toLocalDate().isAfter(m.getEndDate())
                );

        // 2. Если абонемента нет или он не покрывает дату посещения
        if (!hasValidMembership) {
            throw new IllegalStateException(
                    "На дату " + visitDate.toLocalDate() +
                            " у клиента нет действующего абонемента"
            );
        }

        // Остальные проверки (тренер и программа)
        if (visit.getProgram() != null) {
            if (visit.getTrainer() == null) {
                throw new IllegalArgumentException("Для программы требуется тренер");
            }

            boolean isTrainerValid = visit.getTrainer().getPrograms().stream()
                    .anyMatch(p -> p.getId().equals(visit.getProgram().getId()));

            if (!isTrainerValid) {
                throw new IllegalArgumentException("Тренер не сертифицирован для программы");
            }
        }

        return visitRepository.save(visit);
    }

    @Transactional
    public void deleteVisit(Long id) {
        visitRepository.deleteById(id);
    }

    public List<Visit> searchVisits(
            Long clientId,
            LocalDate startDate,
            LocalDate endDate,
            String programName,
            String trainerName,
            Integer minDuration,
            Integer maxDuration) {

        Specification<Visit> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтр по клиенту (если указан)
            if(clientId != null) {
                predicates.add(cb.equal(root.get("client").get("id"), clientId));
            }

            // Остальные фильтры остаются без изменений
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("visitDate"),
                        startDate.atStartOfDay()
                ));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("visitDate"),
                        endDate.atTime(LocalTime.MAX)
                ));
            }
            if (StringUtils.hasText(programName)) {
                predicates.add(cb.like(
                        cb.lower(root.get("program").get("name")),
                        "%" + programName.toLowerCase() + "%"
                ));
            }
            if (StringUtils.hasText(trainerName)) {
                predicates.add(cb.like(
                        cb.lower(root.get("trainer").get("fullName")),
                        "%" + trainerName.toLowerCase() + "%"
                ));
            }
            if (minDuration != null) {
                predicates.add(cb.ge(root.get("program").get("duration"), minDuration));
            }
            if (maxDuration != null) {
                predicates.add(cb.le(root.get("program").get("duration"), maxDuration));
            }

            query.orderBy(cb.desc(root.get("visitDate")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return visitRepository.findAll(spec);
    }
}