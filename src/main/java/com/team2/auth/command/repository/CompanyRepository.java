package com.team2.auth.command.repository;

import com.team2.auth.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

    Optional<Company> findTopByOrderByCompanyIdAsc();
}
