package com.fitness.repository;

import com.fitness.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.memberships WHERE c.id IN :ids")
    List<Client> findAllWithMemberships(@Param("ids") List<Long> ids);
    @Query("SELECT c FROM Client c ORDER BY FUNCTION('SPLIT_PART', c.fullName, ' ', 1) ASC")
    List<Client> findAllSortedByLastName();
}