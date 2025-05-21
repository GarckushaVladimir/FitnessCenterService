package com.fitness.controller;

import com.fitness.model.TrainingProgram;
import com.fitness.service.TrainingProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
//@RequiredArgsConstructor
@RequestMapping("/programs")
public class TrainingProgramController {
    private final TrainingProgramService programService;

    public TrainingProgramController(TrainingProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    public String listPrograms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type, // Новый параметр
            @RequestParam(required = false) Integer minDuration, // Новые параметры
            @RequestParam(required = false) Integer maxDuration,
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

        Page<TrainingProgram> programs = programService.searchPrograms(
                search,
                type,
                minDuration,
                maxDuration,
                pageable
        );

        model.addAttribute("title", "Программы");
        model.addAttribute("content", "programs/list");
        model.addAttribute("programs", programs);
        model.addAttribute("sort", sort);
        model.addAttribute("search", search);
        model.addAttribute("allTypes", programService.getAllProgramTypes());
        model.addAttribute("type", type);
        model.addAttribute("minDuration", minDuration);
        model.addAttribute("maxDuration", maxDuration);

        return "layout";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("title", "Новая программа");
        model.addAttribute("content", "programs/form");
        model.addAttribute("program", new TrainingProgram());
        return "layout";
    }

    @PostMapping("/save")
    public String saveProgram(@ModelAttribute TrainingProgram program) {
        programService.saveProgram(program);
        return "redirect:/programs";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Редактирование программы");
        model.addAttribute("content", "programs/form");
        model.addAttribute("program", programService.getProgramById(id));
        return "layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
        return "redirect:/programs";
    }
}