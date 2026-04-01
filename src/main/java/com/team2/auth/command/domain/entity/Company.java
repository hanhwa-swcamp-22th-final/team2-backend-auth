package com.team2.auth.command.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "company_address_en", length = 500)
    private String companyAddressEn;

    @Column(name = "company_address_kr", length = 500)
    private String companyAddressKr;

    @Column(name = "company_tel", length = 50)
    private String companyTel;

    @Column(name = "company_fax", length = 50)
    private String companyFax;

    @Column(name = "company_email", length = 255)
    private String companyEmail;

    @Column(name = "company_website", length = 255)
    private String companyWebsite;

    @Column(name = "company_seal_image_url", length = 500)
    private String companySealImageUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Company(String companyName, String companyAddressEn, String companyAddressKr,
                   String companyTel, String companyFax, String companyEmail,
                   String companyWebsite, String companySealImageUrl) {
        this.companyName = companyName;
        this.companyAddressEn = companyAddressEn;
        this.companyAddressKr = companyAddressKr;
        this.companyTel = companyTel;
        this.companyFax = companyFax;
        this.companyEmail = companyEmail;
        this.companyWebsite = companyWebsite;
        this.companySealImageUrl = companySealImageUrl;
    }

    public void updateInfo(String companyName, String companyAddressEn, String companyAddressKr,
                           String companyTel, String companyFax, String companyEmail,
                           String companyWebsite, String companySealImageUrl) {
        if (companyName != null) this.companyName = companyName;
        if (companyAddressEn != null) this.companyAddressEn = companyAddressEn;
        if (companyAddressKr != null) this.companyAddressKr = companyAddressKr;
        if (companyTel != null) this.companyTel = companyTel;
        if (companyFax != null) this.companyFax = companyFax;
        if (companyEmail != null) this.companyEmail = companyEmail;
        if (companyWebsite != null) this.companyWebsite = companyWebsite;
        if (companySealImageUrl != null) this.companySealImageUrl = companySealImageUrl;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
