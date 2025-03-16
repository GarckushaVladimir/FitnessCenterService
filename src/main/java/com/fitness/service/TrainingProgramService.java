package com.fitness.service;

import com.fitness.model.TrainingProgram;
import com.fitness.repository.TrainingProgramRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingProgramService {
    private final TrainingProgramRepository programRepository;

    public TrainingProgramService(TrainingProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public List<TrainingProgram> getAllPrograms() {
        return programRepository.findAll();
    }

    public TrainingProgram getProgramById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found"));
    }

    @Transactional
    public TrainingProgram saveProgram(TrainingProgram program) {
        return programRepository.save(program);
    }

    @Transactional
    public void deleteProgram(Long id) {
        programRepository.deleteById(id);
    }
}