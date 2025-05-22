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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String programName,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            Model model) {

        boolean hasFilters = startDate != null || endDate != null
                || programName != null || trainerName != null
                || minDuration != null || maxDuration != null;

        // Если есть хотя бы один параметр фильтрации
        if (startDate != null || endDate != null ||
                programName != null || trainerName != null ||
                minDuration != null || maxDuration != null) {

            // Логика фильтрации
            List<Client> clients = clientService.getAllClientsWithFilteredVisits(
                    startDate,
                    endDate,
                    programName,
                    trainerName,
                    minDuration,
                    maxDuration
            );

            model.addAttribute("clients", clients);
            model.addAttribute("page", null); // Отключаем пагинацию

        } else {
            // Логика пагинации
            PageRequest pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
            Page<Client> clientsPage = clientService.getClientsWithVisits(pageable);

            clientsPage.getContent().forEach(client ->
                    client.getVisits().forEach(visit -> {
                        Hibernate.initialize(visit.getTrainer());
                        Hibernate.initialize(visit.getProgram());
                    })
            );

            model.addAttribute("page", clientsPage);
            model.addAttribute("clients", clientsPage.getContent());
        }

        // Общие атрибуты для всех случаев
        model.addAttribute("title", "Все посещения");
        model.addAttribute("content", "visits/all");
        model.addAttribute("allPrograms", programService.getAllPrograms());
        model.addAttribute("allTrainers", trainerService.getAllTrainers());

        // Сохраняем параметры фильтрации для отображения в форме
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("programName", programName);
        model.addAttribute("trainerName", trainerName);
        model.addAttribute("minDuration", minDuration);
        model.addAttribute("maxDuration", maxDuration);
        model.addAttribute("hasFilters", hasFilters);


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
            @RequestParam Long programId) {
        return trainerService.getTrainersByProgram(programId)
                .stream()
                .map(t -> new TrainerDTO(t.getId(), t.getFullName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Visit visit = visitService.getVisitById(id);
        model.addAttribute("visit", visit);
        model.addAttribute("client", visit.getClient());
        model.addAttribute("programs", programService.getAllPrograms());
        model.addAttribute("trainers", trainerService.getAllTrainers());
        return "visits/form";
    }

    @GetMapping("/client/{clientId}")
    public String listClientVisits(@PathVariable Long clientId, Model model) {
        Client client = clientService.getClientById(clientId);
        List<Visit> visits = visitService.getVisitsByClientId(clientId);

        model.addAttribute("title", "Посещения клиента");
        model.addAttribute("content", "visits/list");
        model.addAttribute("client", client);
        model.addAttribute("visits", visits);

        return "layout";
    }
}