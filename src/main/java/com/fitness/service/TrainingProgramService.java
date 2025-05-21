package com.fitness.service;

import com.fitness.model.TrainingProgram;
import com.fitness.repository.TrainingProgramRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
    public Page<TrainingProgram> searchPrograms(
            String search,
            String type,
            Integer minDuration,
            Integer maxDuration,
            Pageable pageable) {

        Specification<TrainingProgram> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Поиск по названию
            if (StringUtils.hasText(search)) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + search.toLowerCase() + "%"
                ));
            }

            // Фильтр по типу
            if (StringUtils.hasText(type)) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            // Фильтр по длительности
            if (minDuration != null) {
                predicates.add(cb.ge(root.get("duration"), minDuration));
            }
            if (maxDuration != null) {
                predicates.add(cb.le(root.get("duration"), maxDuration));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
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