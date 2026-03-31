package com.team2.auth.command.controller;

import com.team2.auth.dto.UpdateCompanyRequest;
import com.team2.auth.entity.Company;
import com.team2.auth.command.service.CompanyCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyCommandController {

    private final CompanyCommandService companyCommandService;

    @PutMapping
    public ResponseEntity<Company> updateCompany(@RequestBody UpdateCompanyRequest request) {
        return ResponseEntity.ok(companyCommandService.updateCompany(request));
    }
}
