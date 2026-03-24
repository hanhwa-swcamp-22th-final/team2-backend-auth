package com.team2.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyRequest {
    private String name;
    private String addressEn;
    private String addressKr;
    private String tel;
    private String fax;
    private String email;
    private String website;
    private String sealImageUrl;
}
