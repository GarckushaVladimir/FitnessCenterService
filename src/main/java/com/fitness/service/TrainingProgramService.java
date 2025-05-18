package com.fitness.service;

import com.fitness.model.TrainingProgram;
import com.fitness.repository.TrainingProgramRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class TrainingProgramService {
    private static TrainingProgramRepository programRepository = null;

    public TrainingProgramService(TrainingProgramRepository programRepository) {
        TrainingProgramService.programRepository = programRepository;
    }

    public List<TrainingProgram> getAllPrograms() {
        return programRepository.findAll();
    }

    public TrainingProgram getProgramById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found"));
    }
    public List<String> getAllProgramTypes() {
        return programRepository.findAll()
                .stream()
                .map(TrainingProgram::getType)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveProgram(TrainingProgram program) {
        programRepository.save(program);
    }

    @Transactional
    public void deleteProgram(Long id) {
        programRepository.deleteById(id);
    }
}