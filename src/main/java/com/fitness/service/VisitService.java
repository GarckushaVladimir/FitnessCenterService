package com.fitness.service;

import com.fitness.model.Membership;
import com.fitness.model.Visit;
import com.fitness.repository.VisitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        // Проверка активного абонемента
        boolean hasActiveMembership = visit.getClient().getMemberships().stream()
                .anyMatch(Membership::isActive);

        if (!hasActiveMembership) {
            throw new IllegalStateException("У клиента нет активного абонемента");
        }

        // Проверка программы и тренера
        if (visit.getProgram() != null) {
            // Если программа выбрана, тренер обязателен
            if (visit.getTrainer() == null) {
                throw new IllegalArgumentException("Для программы '"
                        + visit.getProgram().getName()
                        + "' требуется тренер");
            }

            // Проверяем, что тренер имеет доступ к программе
            boolean isTrainerValid = visit.getTrainer().getPrograms().stream()
                    .anyMatch(p -> p.getId().equals(visit.getProgram().getId()));

            if (!isTrainerValid) {
                throw new IllegalArgumentException("Тренер '"
                        + visit.getTrainer().getFullName()
                        + "' не сертифицирован для программы '"
                        + visit.getProgram().getName() + "'");
            }
        }

        // Очистка тренера, если передан некорректный ID
        if (visit.getTrainer() != null && visit.getTrainer().getId() == null) {
            visit.setTrainer(null);
        }

        return visitRepository.save(visit);
    }

    @Transactional
    public void deleteVisit(Long id) {
        visitRepository.deleteById(id);
    }
}