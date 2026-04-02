package com.team2.auth.command.application.service;

import com.team2.auth.command.application.dto.UpdateCompanyRequest;
import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.command.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyCommandService {

    private final CompanyRepository companyRepository;
    private final S3FileService s3FileService;

    public Company updateCompany(UpdateCompanyRequest request) {
        Company company = companyRepository.findTopByOrderByCompanyIdAsc()
                .orElseThrow(() -> new IllegalArgumentException("회사 정보를 찾을 수 없습니다."));
        company.updateInfo(
                request.getName(),
                request.getAddressEn(),
                request.getAddressKr(),
                request.getTel(),
                request.getFax(),
                request.getEmail(),
                request.getWebsite(),
                request.getSealImageUrl()
        );
        return company;
    }

    public String uploadSealImage(MultipartFile file) {
        Company company = companyRepository.findTopByOrderByCompanyIdAsc()
                .orElseThrow(() -> new IllegalArgumentException("회사 정보를 찾을 수 없습니다."));

        String imageUrl = s3FileService.upload("company/seal", file);
        company.updateInfo(null, null, null, null, null, null, null, imageUrl);
        return imageUrl;
    }
}
