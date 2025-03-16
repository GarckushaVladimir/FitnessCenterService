package com.fitness.service;

import com.fitness.model.Visit;
import com.fitness.repository.VisitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
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
        return visitRepository.save(visit);
    }

    @Transactional
    public void deleteVisit(Long id) {
        visitRepository.deleteById(id);
    }
}