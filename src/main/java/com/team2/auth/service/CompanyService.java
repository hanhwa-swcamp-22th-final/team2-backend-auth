package com.team2.auth.service;

import com.team2.auth.dto.UpdateCompanyRequest;
import com.team2.auth.entity.Company;
import com.team2.auth.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    public Company getCompany() {
        return companyRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalArgumentException("회사 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public Company updateCompany(UpdateCompanyRequest request) {
        Company company = getCompany();
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
