package com.fitness.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "trainers")
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private Integer experience;

    @ManyToMany
    @JoinTable(
            name = "trainer_program",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    private List<TrainingProgram> programs = new ArrayList<>();

    public void setPrograms(List<TrainingProgram> programs) {
        this.programs = programs;
    }

    public List<TrainingProgram> getPrograms() {
        return programs;
    }
}