package com.team2.auth.entity;

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
    private Integer id;

    @Column(name = "company_name", nullable = false, length = 200)
    private String name;

    @Column(name = "company_address_en", length = 500)
    private String addressEn;

    @Column(name = "company_address_kr", length = 500)
    private String addressKr;

    @Column(name = "company_tel", length = 50)
    private String tel;

    @Column(name = "company_fax", length = 50)
    private String fax;

    @Column(name = "company_email", length = 255)
    private String email;

    @Column(name = "company_website", length = 255)
    private String website;

    @Column(name = "company_seal_image_url", length = 500)
    private String sealImageUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Company(String name, String addressEn, String addressKr,
                   String tel, String fax, String email,
                   String website, String sealImageUrl) {
        this.name = name;
        this.addressEn = addressEn;
        this.addressKr = addressKr;
        this.tel = tel;
        this.fax = fax;
        this.email = email;
        this.website = website;
        this.sealImageUrl = sealImageUrl;
    }

    public void updateInfo(String name, String addressEn, String addressKr,
                           String tel, String fax, String email,
                           String website, String sealImageUrl) {
        if (name != null) this.name = name;
        if (addressEn != null) this.addressEn = addressEn;
        if (addressKr != null) this.addressKr = addressKr;
        if (tel != null) this.tel = tel;
        if (fax != null) this.fax = fax;
        if (email != null) this.email = email;
        if (website != null) this.website = website;
        if (sealImageUrl != null) this.sealImageUrl = sealImageUrl;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
