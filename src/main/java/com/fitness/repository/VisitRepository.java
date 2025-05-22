package com.fitness.repository;

import com.fitness.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long>, JpaSpecificationExecutor<Visit> {
    List<Visit> findByClientId(Long clientId);
    @Query("SELECT v FROM Visit v WHERE v.client.id = :clientId ORDER BY v.visitDate DESC")
    List<Visit> findByClientIdOrderByVisitDateDesc(@Param("clientId") Long clientId);
}