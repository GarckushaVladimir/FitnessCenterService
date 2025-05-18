package com.fitness.repository;

import com.fitness.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    @Query("SELECT DISTINCT t FROM Trainer t " +
            "JOIN FETCH t.programs p " +
            "WHERE p.id = :programId")
    List<Trainer> findTrainersByProgramId(@Param("programId") Long programId);
    @Query("SELECT t FROM Trainer t LEFT JOIN FETCH t.programs WHERE t.id = :id")
    Optional<Trainer> findByIdWithPrograms(@Param("id") Long id);
}