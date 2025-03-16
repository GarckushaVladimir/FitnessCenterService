package com.fitness.controller;

import com.fitness.model.Membership;
import com.fitness.service.ClientService;
import com.fitness.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
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
        model.addAttribute("title", "Новый абонемент");
        model.addAttribute("content", "memberships/form");
        model.addAttribute("membership", new Membership());
        model.addAttribute("clientId", clientId);
        return "layout";
    }

    @PostMapping("/save")
    public String saveMembership(
            @ModelAttribute Membership membership,
            @RequestParam("clientId") Long clientId
    ) {
        membership.setClient(clientService.getClientById(clientId));
        membership.setActive(LocalDate.now().isBefore(membership.getEndDate()));
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