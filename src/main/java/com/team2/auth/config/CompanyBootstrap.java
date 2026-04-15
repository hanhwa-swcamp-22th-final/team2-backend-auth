package com.team2.auth.config;

import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.command.domain.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 자사(Company) 시드 데이터 idempotent 보장.
 * prod profile 에서는 spring.sql.init.mode=never 라 data.sql 이 돌지 않아
 * company 테이블이 비면 PI/PO/CI PDF 발행 시 자사 정보가 공백으로 출력된다.
 *
 * 1) 테이블이 비어있으면 INSERT.
 * 2) 첫 행이 존재하나 핵심 필드(companyName/addressEn/tel/email) 어느 하나라도
 *    비어있으면 시드값으로 UPDATE. (JPA ddl-auto=update 가 빈 row 를 만들었거나
 *    과거 미완성 시드 케이스 모두 커버)
 *
 * ApplicationReadyEvent 사용 — 이전 CommandLineRunner 가 일부 환경에서 트랜잭션
 * 컨텍스트 누락으로 save 가 commit 되지 않은 케이스 회피.
 */
@Component
public class CompanyBootstrap {

    private static final Logger log = LoggerFactory.getLogger(CompanyBootstrap.class);

    private static final String DEFAULT_NAME = "한화솔루션";
    private static final String DEFAULT_ADDRESS_EN = "86 Cheonggyecheon-ro, Jung-gu, Seoul";
    private static final String DEFAULT_ADDRESS_KR = "서울특별시 중구 청계천로 86";
    private static final String DEFAULT_TEL = "02-729-2700";
    private static final String DEFAULT_FAX = "02-729-2799";
    // 실제 SMTP 발신자 (MAIL_USERNAME) 와 일치시켜 PDF/문서 표시·이메일 회신 정합 확보
    private static final String DEFAULT_EMAIL = "teamsalesboost@gmail.com";
    private static final String DEFAULT_WEBSITE = "https://www.hanwhasolutions.com";

    private final CompanyRepository companyRepository;

    public CompanyBootstrap(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedCompanyOnReady() {
        Company existing = companyRepository.findTopByOrderByCompanyIdAsc().orElse(null);

        if (existing == null) {
            companyRepository.saveAndFlush(buildSeed());
            log.info("CompanyBootstrap: inserted default company info");
            return;
        }

        boolean missingCore =
                isBlank(existing.getCompanyName())
                || isBlank(existing.getCompanyAddressEn())
                || isBlank(existing.getCompanyTel())
                || isBlank(existing.getCompanyEmail());

        if (missingCore) {
            existing.updateInfo(
                    isBlank(existing.getCompanyName()) ? DEFAULT_NAME : null,
                    isBlank(existing.getCompanyAddressEn()) ? DEFAULT_ADDRESS_EN : null,
                    isBlank(existing.getCompanyAddressKr()) ? DEFAULT_ADDRESS_KR : null,
                    isBlank(existing.getCompanyTel()) ? DEFAULT_TEL : null,
                    isBlank(existing.getCompanyFax()) ? DEFAULT_FAX : null,
                    isBlank(existing.getCompanyEmail()) ? DEFAULT_EMAIL : null,
                    isBlank(existing.getCompanyWebsite()) ? DEFAULT_WEBSITE : null,
                    null
            );
            companyRepository.saveAndFlush(existing);
            log.info("CompanyBootstrap: filled blank fields on existing company row id={}", existing.getCompanyId());
        } else {
            log.debug("CompanyBootstrap: company info already populated, skipping");
        }
    }

    private Company buildSeed() {
        return Company.builder()
                .companyName(DEFAULT_NAME)
                .companyAddressEn(DEFAULT_ADDRESS_EN)
                .companyAddressKr(DEFAULT_ADDRESS_KR)
                .companyTel(DEFAULT_TEL)
                .companyFax(DEFAULT_FAX)
                .companyEmail(DEFAULT_EMAIL)
                .companyWebsite(DEFAULT_WEBSITE)
                .build();
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
