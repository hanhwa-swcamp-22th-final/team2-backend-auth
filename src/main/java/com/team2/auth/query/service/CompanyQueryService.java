package com.team2.auth.query.service;

import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.query.mapper.CompanyQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyQueryService {

    private final CompanyQueryMapper companyQueryMapper;

    public Company getCompany() {
        Company company = companyQueryMapper.findFirst();
        if (company == null) {
            throw new IllegalArgumentException("회사 정보를 찾을 수 없습니다.");
        }
        return company;
    }
}
