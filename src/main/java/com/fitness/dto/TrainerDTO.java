package com.fitness.dto;

public class TrainerDTO {
    private Long id;
    private String fullName;

    // Конструкторы
    public TrainerDTO() {}

    public TrainerDTO(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}