package com.fitness.controller;

import com.fitness.model.TrainingProgram;
import com.fitness.service.TrainingProgramService;
import lombok.RequiredArgsConstructor;
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
    public String listPrograms(Model model) {
        model.addAttribute("title", "Программы тренировок");
        model.addAttribute("content", "programs/list");
        model.addAttribute("programs", programService.getAllPrograms());
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