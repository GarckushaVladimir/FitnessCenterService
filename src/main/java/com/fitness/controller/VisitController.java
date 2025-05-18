package com.fitness.controller;

import com.fitness.dto.TrainerDTO;
import com.fitness.model.Client;
import com.fitness.model.Trainer;
import com.fitness.model.Visit;
import com.fitness.service.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
//@RequiredArgsConstructor
@RequestMapping("/visits")
public class VisitController {
    private final VisitService visitService;
    private final ClientService clientService;
    private final TrainingProgramService programService;
    private final TrainerService trainerService;

    public VisitController(VisitService visitService, ClientService clientService, TrainingProgramService programService, TrainerService trainerService) {
        this.visitService = visitService;
        this.clientService = clientService;
        this.programService = programService;
        this.trainerService = trainerService;
    }

    @GetMapping
    public String listAllVisits(Model model) {
        List<Client> clients = clientService.getAllClientsWithVisits();

        // Инициализация связанных сущностей
        clients.forEach(client ->
                client.getVisits().forEach(visit -> {
                    if(visit.getTrainer() != null) {
                        Hibernate.initialize(visit.getTrainer()); // Явная инициализация тренера
                    }
                    if(visit.getProgram() != null) {
                        Hibernate.initialize(visit.getProgram()); // Инициализация программы
                    }
                })
        );

        model.addAttribute("title", "Все посещения");
        model.addAttribute("content", "visits/all");
        model.addAttribute("clients", clients);
        return "layout";
    }

    @GetMapping("/client/{clientId}")
    public String listVisits(@PathVariable Long clientId, Model model) {
        model.addAttribute("title", "Посещения клиента");
        model.addAttribute("content", "visits/list");
        model.addAttribute("client", clientService.getClientById(clientId));
        model.addAttribute("visits", visitService.getVisitsByClientId(clientId));
        return "layout";
    }

    @GetMapping("/new/{clientId}")
    public String showForm(@PathVariable Long clientId, Model model) {
        Client client = clientService.getClientById(clientId); // Получаем клиента
        model.addAttribute("trainers", trainerService.getAllTrainers());
        model.addAttribute("visit", new Visit());
        model.addAttribute("client", client); // Добавляем клиента в модель
        model.addAttribute("programs", programService.getAllPrograms());
        model.addAttribute("title", "Новое посещение");
        model.addAttribute("content", "visits/form");
        return "layout";
    }

    @PostMapping("/save")
    public String saveVisit(
            @ModelAttribute Visit visit,
            @RequestParam("clientId") Long clientId
    ) {
        Client client = clientService.getClientById(clientId);
        visit.setClient(client);

        // Тренер будет привязан автоматически через th:field
        visitService.saveVisit(visit);

        return "redirect:/visits/client/" + clientId;
    }

    @PostMapping("/delete/{id}")
    public String deleteVisit(@PathVariable Long id) {
        Long clientId = visitService.getVisitById(id).getClient().getId();
        visitService.deleteVisit(id);
        return "redirect:/visits/client/" + clientId;
    }
    @GetMapping("/trainers/by-program")
    @ResponseBody
    public List<TrainerDTO> getTrainersByProgram(
            @RequestParam Long programId
    ) {
        return trainerService.getTrainersByProgram(programId)
                .stream()
                .map(trainer -> new TrainerDTO(
                        trainer.getId(),
                        trainer.getFullName()
                ))
                .collect(Collectors.toList());
    }
}