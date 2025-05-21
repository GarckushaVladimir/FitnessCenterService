package com.fitness.service;

import com.fitness.model.Trainer;
import com.fitness.model.TrainingProgram;
import com.fitness.repository.TrainerRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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

    public Page<Trainer> searchTrainers(String search, Long programId,Integer minExp,
                                        Integer maxExp, Pageable pageable) {
        Specification<Trainer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Поиск по имени
            if (StringUtils.hasText(search)) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%"));
            }

            // Фильтр по программе
            if (programId != null) {
                Join<Trainer, TrainingProgram> programJoin = root.join("programs");
                predicates.add(cb.equal(programJoin.get("id"), programId));
            }
            // Фильтр по стажу
            if (minExp != null) {
                predicates.add(cb.ge(root.get("experience"), minExp)); // ge = greater or equal
            }
            if (maxExp != null) {
                predicates.add(cb.le(root.get("experience"), maxExp)); // le = less or equal
            }

            return cb.and(predicates.toArray(new Predicate[0]));
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