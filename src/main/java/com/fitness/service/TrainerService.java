package com.fitness.service;

import com.fitness.model.Trainer;
import com.fitness.model.TrainingProgram;
import com.fitness.repository.TrainerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
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
        return trainerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
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