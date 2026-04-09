package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.UpdateCompanyRequest;
import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.command.application.service.CompanyCommandService;
import com.team2.auth.query.controller.CompanyQueryController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "회사 정보 수정", description = "회사 정보 수정 및 직인 이미지 업로드 API")
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyCommandController {

    private final CompanyCommandService companyCommandService;

    @Operation(summary = "회사 정보 수정", description = "회사 기본 정보(이름, 주소, 연락처 등)를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회사 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PutMapping
    public ResponseEntity<EntityModel<Company>> updateCompany(@RequestBody UpdateCompanyRequest request) {
        Company company = companyCommandService.updateCompany(request);
        return ResponseEntity.ok(EntityModel.of(company,
                linkTo(methodOn(CompanyQueryController.class).getCompany()).withSelfRel()));
    }

    @Operation(summary = "직인 이미지 업로드", description = "회사 직인 이미지를 S3에 업로드하고 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "직인 이미지 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 실패")
    })
    @PostMapping("/seal")
    public ResponseEntity<Map<String, String>> uploadSeal(
            @Parameter(description = "업로드할 직인 이미지 파일")
            @RequestParam(name = "file", "file") MultipartFile file) {
        String imageUrl = companyCommandService.uploadSealImage(file);
        return ResponseEntity.ok(Map.of(
                "companySealImageUrl", imageUrl,
                "message", "업로드 완료"
        ));
    }
}
