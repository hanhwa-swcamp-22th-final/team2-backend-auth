package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.query.service.CompanyQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "회사 정보 조회", description = "회사 정보 조회 API")
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyQueryController {

    private final CompanyQueryService companyQueryService;

    @Operation(summary = "회사 정보 조회", description = "회사 기본 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회사 정보를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<EntityModel<Company>> getCompany() {
        Company company = companyQueryService.getCompany();
        return ResponseEntity.ok(EntityModel.of(company,
                linkTo(methodOn(CompanyQueryController.class).getCompany()).withSelfRel()));
    }
}
