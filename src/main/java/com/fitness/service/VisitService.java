package com.fitness.service;

import com.fitness.model.Client;
import com.fitness.model.Membership;
import com.fitness.model.Visit;
import com.fitness.repository.VisitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
//@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;

    public VisitService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    // Добавляем метод для получения посещений по ID клиента
    public List<Visit> getVisitsByClientId(Long clientId) {
        return visitRepository.findByClientId(clientId);
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
}