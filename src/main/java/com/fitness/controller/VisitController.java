package com.fitness.controller;

import com.fitness.model.Client;
import com.fitness.model.Visit;
import com.fitness.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
//@RequiredArgsConstructor
@RequestMapping("/visits")
public class VisitController {
    private final VisitService visitService;
    private final ClientService clientService;
    private final TrainingProgramService programService;

    public VisitController(VisitService visitService, ClientService clientService, TrainingProgramService programService) {
        this.visitService = visitService;
        this.clientService = clientService;
        this.programService = programService;
    }

    @GetMapping
    public String listAllVisits(Model model) {
        model.addAttribute("title", "Все посещения");
        model.addAttribute("content", "visits/all");
        model.addAttribute("clients", clientService.getAllClientsWithVisits());
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
            @RequestParam("clientId") Long clientId // Получаем clientId из формы
    ) {
        visit.setClient(clientService.getClientById(clientId));
        visitService.saveVisit(visit);
        return "redirect:/visits/client/" + clientId;
    }

    @PostMapping("/delete/{id}")
    public String deleteVisit(@PathVariable Long id) {
        Long clientId = visitService.getVisitById(id).getClient().getId();
        visitService.deleteVisit(id);
        return "redirect:/visits/client/" + clientId;
    }
}