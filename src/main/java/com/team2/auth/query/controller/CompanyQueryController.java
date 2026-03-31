package com.team2.auth.query.controller;

import com.team2.auth.entity.Company;
import com.team2.auth.query.service.CompanyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyQueryController {

    private final CompanyQueryService companyQueryService;

    @GetMapping
    public ResponseEntity<Company> getCompany() {
        return ResponseEntity.ok(companyQueryService.getCompany());
    }
}
