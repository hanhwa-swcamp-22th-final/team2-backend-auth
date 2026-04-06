package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회사 정보 수정 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyRequest {
    @Schema(description = "회사명", example = "플레이데이터")
    private String name;

    @Schema(description = "영문 주소", example = "123 Gangnam-daero, Seoul")
    private String addressEn;

    @Schema(description = "한글 주소", example = "서울특별시 강남구 강남대로 123")
    private String addressKr;

    @Schema(description = "전화번호", example = "02-1234-5678")
    private String tel;

    @Schema(description = "팩스번호", example = "02-1234-5679")
    private String fax;

    @Schema(description = "이메일", example = "info@playdata.io")
    private String email;

    @Schema(description = "웹사이트 URL", example = "https://playdata.io")
    private String website;

    @Schema(description = "직인 이미지 URL", example = "https://s3.amazonaws.com/bucket/seal.png")
    private String sealImageUrl;
}
