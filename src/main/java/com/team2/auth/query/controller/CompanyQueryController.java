package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.query.service.CompanyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyQueryController {

    private final CompanyQueryService companyQueryService;

    @GetMapping
    public ResponseEntity<EntityModel<Company>> getCompany() {
        Company company = companyQueryService.getCompany();
        return ResponseEntity.ok(EntityModel.of(company,
                linkTo(methodOn(CompanyQueryController.class).getCompany()).withSelfRel()));
    }
}
