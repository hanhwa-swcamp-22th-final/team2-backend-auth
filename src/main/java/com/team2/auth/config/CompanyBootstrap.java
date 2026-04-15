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
 *
 * 1) 테이블이 비어있으면 INSERT.
 * 2) 첫 행이 존재하나 핵심 필드(companyName/address) 가 비어있으면 시드값으로 UPDATE.
 *    (JPA ddl-auto=update 가 빈 row 를 만들어둔 상태 / 과거 미완성 시드 케이스 모두 커버)
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
        Company existing = companyRepository.findTopByOrderByCompanyIdAsc().orElse(null);

        if (existing == null) {
            companyRepository.save(buildSeed());
            log.info("CompanyBootstrap: inserted default company info");
            return;
        }

        if (isBlank(existing.getCompanyName()) || isBlank(existing.getCompanyAddressEn())) {
            existing.updateInfo(
                    "한화솔루션",
                    "86 Cheonggyecheon-ro, Jung-gu, Seoul",
                    "서울특별시 중구 청계천로 86",
                    "02-729-2700",
                    "02-729-2799",
                    "contact@hanwha.com",
                    "https://www.hanwhasolutions.com",
                    null
            );
            companyRepository.save(existing);
            log.info("CompanyBootstrap: filled empty company row with default info");
        }
    }

    private Company buildSeed() {
        return Company.builder()
                .companyName("한화솔루션")
                .companyAddressEn("86 Cheonggyecheon-ro, Jung-gu, Seoul")
                .companyAddressKr("서울특별시 중구 청계천로 86")
                .companyTel("02-729-2700")
                .companyFax("02-729-2799")
                .companyEmail("contact@hanwha.com")
                .companyWebsite("https://www.hanwhasolutions.com")
                .build();
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
