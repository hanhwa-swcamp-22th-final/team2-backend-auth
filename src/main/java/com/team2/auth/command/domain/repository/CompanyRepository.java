package com.team2.auth.command.domain.repository;

import com.team2.auth.command.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

    Optional<Company> findTopByOrderByCompanyIdAsc();
}
