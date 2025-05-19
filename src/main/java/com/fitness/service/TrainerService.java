package com.fitness.service;

import com.fitness.model.Trainer;
import com.fitness.model.TrainingProgram;
import com.fitness.repository.TrainerRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingProgramService programService;

    public TrainerService(TrainerRepository trainerRepository, TrainingProgramService programService) {
        this.trainerRepository = trainerRepository;
        this.programService = programService;
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public Trainer getTrainerById(Long id) {
        return trainerRepository.findByIdWithPrograms(id)
                .orElseThrow(() -> new RuntimeException("Тренер не найден"));
    }

    public List<Trainer> getTrainersByProgram(Long programId) {
        if (programId == null) return Collections.emptyList();

        List<Trainer> trainers = trainerRepository.findTrainersByProgramId(programId);

        // Очистка циклических ссылок
        trainers.forEach(trainer -> {
            trainer.setPrograms(null); // Исключаем программы из ответа
        });

        return trainers;
    }

    public Page<Trainer> searchTrainers(String search, Pageable pageable) {
        Specification<Trainer> spec = (root, query, cb) -> {
            if (search == null || search.isEmpty()) return null;
            return cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%");
        };
        return trainerRepository.findAll(spec, pageable);
    }

    @Transactional
    public void saveTrainer(Trainer trainer, List<Long> programIds) {
        // Преобразуем ID программ в объекты
        List<TrainingProgram> programs = programIds.stream()
                .map(programService::getProgramById)
                .collect(Collectors.toList());

        // Устанавливаем программы для тренера
        trainer.setPrograms(programs);

        // Сохраняем тренера
        trainerRepository.save(trainer);
    }

    @Transactional
    public void deleteTrainer(Long id) {
        trainerRepository.deleteById(id);
    }
}