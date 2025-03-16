package com.fitness.service;

import com.fitness.model.Membership;
import com.fitness.repository.MembershipRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;

    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public List<Membership> getMembershipsByClientId(Long clientId) {
        return membershipRepository.findByClientId(clientId);
    }

    public Membership getMembershipById(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membership not found"));
    }

    @Transactional
    public void saveMembership(Membership membership) {
        membershipRepository.save(membership);
    }

    @Transactional
    public void deleteMembership(Long id) {
        membershipRepository.deleteById(id);
    }
}