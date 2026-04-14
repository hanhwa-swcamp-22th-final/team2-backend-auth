package com.team2.auth.config;

import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.command.domain.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 자사(Company) 시드 데이터 idempotent 보장.
 * prod profile 에서는 spring.sql.init.mode=never 라 data.sql 이 돌지 않아
 * company 테이블이 비면 PI/PO/CI PDF 발행 시 자사 정보가 공백으로 출력된다.
 * 테이블이 비어있을 때만 1행을 INSERT.
 */
@Component
public class CompanyBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CompanyBootstrap.class);

    private final CompanyRepository companyRepository;

    public CompanyBootstrap(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void run(String... args) {
        if (companyRepository.count() > 0) {
            return;
        }
        Company seed = Company.builder()
                .companyName("한화솔루션")
                .companyAddressEn("86 Cheonggyecheon-ro, Jung-gu, Seoul")
                .companyAddressKr("서울특별시 중구 청계천로 86")
                .companyTel("02-729-2700")
                .companyFax("02-729-2799")
                .companyEmail("contact@hanwha.com")
                .companyWebsite("https://www.hanwhasolutions.com")
                .build();
        companyRepository.save(seed);
        log.info("CompanyBootstrap: seeded default company info");
    }
}
