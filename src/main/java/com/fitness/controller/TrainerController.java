package com.fitness.controller;

import com.fitness.model.Trainer;
import com.fitness.service.TrainerService;
import com.fitness.service.TrainingProgramService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/trainers")
public class TrainerController {
    private final TrainerService trainerService;
    private final TrainingProgramService programService;

    public TrainerController(TrainerService trainerService, TrainingProgramService programService) {
        this.trainerService = trainerService;
        this.programService = programService;
    }

    @GetMapping
    public String listTrainers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "fullName,asc") String sort,
            @RequestParam(required = false) String search,
            Model model) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1])
                : Sort.Direction.ASC;

        PageRequest pageable = PageRequest.of(
                page,
                10,
                Sort.by(direction, sortParams[0])
        );

        Page<Trainer> trainers = trainerService.searchTrainers(search, pageable);

        model.addAttribute("title", "Тренеры");
        model.addAttribute("content", "trainers/list");
        model.addAttribute("trainers", trainers);
        model.addAttribute("sort", sort);
        model.addAttribute("search", search);

        return "layout";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("title", "Новый тренер");
        model.addAttribute("content", "trainers/form");
        model.addAttribute("trainer", new Trainer());
        model.addAttribute("allPrograms", programService.getAllPrograms());
        return "layout";
    }

    @PostMapping("/save")
    public String saveTrainer(
            @ModelAttribute Trainer trainer,
            @RequestParam("programIds") List<Long> programIds // Получаем выбранные программы
    ) {
        trainerService.saveTrainer(trainer, programIds);
        return "redirect:/trainers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Trainer trainer = trainerService.getTrainerById(id);
        model.addAttribute("title", "Редактирование тренера");
        model.addAttribute("content", "trainers/form");
        model.addAttribute("trainer", trainer);
        model.addAttribute("allPrograms", programService.getAllPrograms());
        return "layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteTrainer(@PathVariable Long id) {
        trainerService.deleteTrainer(id);
        return "redirect:/trainers";
    }
}