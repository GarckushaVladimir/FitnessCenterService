package com.fitness.controller;

import com.fitness.model.Client;
import com.fitness.model.Membership;
import com.fitness.service.ClientService;
import com.fitness.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
//@RequiredArgsConstructor
@RequestMapping("/memberships")
public class MembershipController {
    private final MembershipService membershipService;
    private final ClientService clientService;

    public MembershipController(MembershipService membershipService, ClientService clientService) {
        this.membershipService = membershipService;
        this.clientService = clientService;
    }

    @GetMapping("/new/{clientId}")
    public String showAddForm(@PathVariable Long clientId, Model model) {
        Client client = clientService.getClientById(clientId); // Получаем клиента
        Membership membership = new Membership(); // Создаем новый абонемент
        membership.setClient(client); // Устанавливаем клиента

        model.addAttribute("membership", membership);
        model.addAttribute("client", client); // Передаем клиента в модель
        model.addAttribute("title", "Новый абонемент");
        model.addAttribute("content", "memberships/form");
        return "layout";
    }

    @PostMapping("/save")
    public String saveMembership(
            @ModelAttribute Membership membership,
            @RequestParam("clientId") Long clientId) { // Получаем clientId из формы

        // Получаем клиента из базы данных
        Client client = clientService.getClientById(clientId);

        // Устанавливаем клиента для абонемента
        membership.setClient(client);

        // Сохраняем абонемент
        membershipService.saveMembership(membership);

        return "redirect:/clients/" + clientId + "/memberships";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Membership membership = membershipService.getMembershipById(id);
        model.addAttribute("title", "Редактирование абонемента");
        model.addAttribute("content", "memberships/form"); // Указывает на шаблон формы
        model.addAttribute("membership", membership);
        model.addAttribute("client", membership.getClient());
        return "layout"; // Используем общий layout
    }

    @PostMapping("/update/{id}")
    public String updateMembership(
            @PathVariable Long id,
            @ModelAttribute Membership membership,
            @RequestParam("clientId") Long clientId // Добавьте параметр clientId
    ) {
        Client client = clientService.getClientById(clientId);
        membership.setClient(client); // Установите клиента
        membershipService.saveMembership(membership);
        return "redirect:/clients/" + clientId + "/memberships";
    }

    @PostMapping("/delete/{id}")
    public String deleteMembership(@PathVariable Long id) {
        Membership membership = membershipService.getMembershipById(id);
        Long clientId = membership.getClient().getId();
        membershipService.deleteMembership(id);
        return "redirect:/clients/" + clientId + "/memberships";
    }
}