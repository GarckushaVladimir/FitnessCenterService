package com.fitness.controller;

import com.fitness.dto.TrainerDTO;
import com.fitness.model.Client;
import com.fitness.model.Trainer;
import com.fitness.model.Visit;
import com.fitness.service.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public String listAllVisits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Пагинация для клиентов
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<Client> clientsPage = clientService.getClientsWithVisits(pageable);

        // Инициализация связанных сущностей для каждого посещения
        clientsPage.getContent().forEach(client ->
                client.getVisits().forEach(visit -> {
                    Hibernate.initialize(visit.getTrainer());
                    Hibernate.initialize(visit.getProgram());
                })
        );

        model.addAttribute("title", "Все посещения");
        model.addAttribute("content", "visits/all");
        model.addAttribute("clients", clientsPage.getContent());
        model.addAttribute("page", clientsPage);
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
        Client client = clientService.getClientById(clientId);

        // Явная инициализация связанных данных
        Hibernate.initialize(client.getMemberships());

        model.addAttribute("trainers", trainerService.getAllTrainers());
        model.addAttribute("visit", new Visit());
        model.addAttribute("client", client);
        model.addAttribute("programs", programService.getAllPrograms());

        return "visits/form";
    }

    @PostMapping("/save")
    public String saveVisit(
            @ModelAttribute Visit visit,
            @RequestParam("clientId") Long clientId,
            Model model) {

        try {
            Client client = clientService.getClientById(clientId);
            visit.setClient(client);
            visitService.saveVisit(visit);
            return "redirect:/visits/client/" + clientId;

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Добавляем сообщение об ошибке в модель
            model.addAttribute("errorMessage", e.getMessage());

            // Возвращаем на форму с сохранением введенных данных
            model.addAttribute("visit", visit);
            model.addAttribute("client", clientService.getClientById(clientId));
            model.addAttribute("programs", programService.getAllPrograms());
            model.addAttribute("trainers", trainerService.getAllTrainers());

            return "visits/form";
        }
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