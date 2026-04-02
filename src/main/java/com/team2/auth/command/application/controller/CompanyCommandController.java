package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.UpdateCompanyRequest;
import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.command.application.service.CompanyCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyCommandController {

    private final CompanyCommandService companyCommandService;

    @PutMapping
    public ResponseEntity<Company> updateCompany(@RequestBody UpdateCompanyRequest request) {
        return ResponseEntity.ok(companyCommandService.updateCompany(request));
    }

    @PostMapping("/seal")
    public ResponseEntity<Map<String, String>> uploadSeal(@RequestParam("file") MultipartFile file) {
        String imageUrl = companyCommandService.uploadSealImage(file);
        return ResponseEntity.ok(Map.of(
                "companySealImageUrl", imageUrl,
                "message", "업로드 완료"
        ));
    }
}
