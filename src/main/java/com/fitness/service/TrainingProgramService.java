package com.fitness.service;

import com.fitness.model.TrainingProgram;
import com.fitness.repository.TrainingProgramRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public Page<TrainingProgram> searchPrograms(String search, Pageable pageable) {
        Specification<TrainingProgram> spec = (root, query, cb) -> {
            if (search == null || search.isEmpty()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
        return programRepository.findAll(spec, pageable);
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