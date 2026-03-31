package com.team2.auth.service;

import com.team2.auth.dto.UpdateCompanyRequest;
import com.team2.auth.entity.Company;
import com.team2.auth.mapper.CompanyQueryMapper;
import com.team2.auth.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyCommandService {

    private final CompanyRepository companyRepository;
    private final CompanyQueryMapper companyQueryMapper;

    public Company updateCompany(UpdateCompanyRequest request) {
        Company company = companyQueryMapper.findFirst();
        if (company == null) {
            throw new IllegalArgumentException("회사 정보를 찾을 수 없습니다.");
        }
        // Re-fetch via JPA to get a managed entity for dirty checking
        Company managedCompany = companyRepository.findById(company.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("회사 정보를 찾을 수 없습니다."));
        managedCompany.updateInfo(
                request.getName(),
                request.getAddressEn(),
                request.getAddressKr(),
                request.getTel(),
                request.getFax(),
                request.getEmail(),
                request.getWebsite(),
                request.getSealImageUrl()
        );
        return managedCompany;
    }
}
