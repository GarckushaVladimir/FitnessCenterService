package com.fitness.controller;

import com.fitness.model.Client;
import com.fitness.service.ClientService;
import com.fitness.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
//@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController {
    private final ClientService clientService;
    private final MembershipService membershipService;

    public ClientController(ClientService clientService, MembershipService membershipService) {
        this.clientService = clientService;
        this.membershipService = membershipService;
    }

    @GetMapping
    public String listClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "fullName,asc") String sort,
            @RequestParam(required = false) String search,
            Model model) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1])
                : Sort.Direction.ASC;

        PageRequest pageable = PageRequest.of(
                page,
                10,
                Sort.by(direction, sortField)
        );

        Page<Client> clients = clientService.searchClients(search, pageable);

        model.addAttribute("title", "Клиенты");
        model.addAttribute("content", "clients/list");
        model.addAttribute("clients", clients);
        model.addAttribute("sort", sort);
        model.addAttribute("search", search);

        return "layout";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("title", "Новый клиент");
        model.addAttribute("content", "clients/form");
        model.addAttribute("client", new Client());
        return "layout";
    }

    @PostMapping("/save")
    public String saveClient(@ModelAttribute Client client) {
        clientService.saveClient(client);
        return "redirect:/clients";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Редактирование клиента");
        model.addAttribute("content", "clients/form");
        model.addAttribute("client", clientService.getClientById(id));
        return "layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return "redirect:/clients";
    }

    @GetMapping("/{id}/memberships")
    public String viewMemberships(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Абонементы клиента");
        model.addAttribute("content", "clients/memberships");
        model.addAttribute("client", clientService.getClientById(id));
        model.addAttribute("memberships", membershipService.getMembershipsByClientId(id));
        return "layout";
    }

    @GetMapping("/active")
    public String listActiveClients(Model model) {
        model.addAttribute("title", "Активные клиенты");
        model.addAttribute("content", "clients/list");
        model.addAttribute("clients", clientService.getClientsWithActiveMemberships());
        return "layout";
    }
}