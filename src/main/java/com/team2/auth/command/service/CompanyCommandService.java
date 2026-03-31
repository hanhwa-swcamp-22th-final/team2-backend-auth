package com.team2.auth.command.service;

import com.team2.auth.dto.UpdateCompanyRequest;
import com.team2.auth.entity.Company;
import com.team2.auth.command.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyCommandService {

    private final CompanyRepository companyRepository;

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
}
