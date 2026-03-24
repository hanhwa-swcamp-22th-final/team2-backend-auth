package com.team2.auth.controller;

import com.team2.auth.dto.UpdateCompanyRequest;
import com.team2.auth.entity.Company;
import com.team2.auth.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<Company> getCompany() {
        return ResponseEntity.ok(companyService.getCompany());
    }

    @PutMapping
    public ResponseEntity<Company> updateCompany(@RequestBody UpdateCompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(request));
    }
}
