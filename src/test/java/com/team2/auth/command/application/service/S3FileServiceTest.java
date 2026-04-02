package com.team2.auth.command.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3FileServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3FileService s3FileService;

    private void setBucket(String bucket) throws Exception {
        Field field = S3FileService.class.getDeclaredField("bucket");
        field.setAccessible(true);
        field.set(s3FileService, bucket);
    }

    @Test
    @DisplayName("파일 업로드 성공 시 S3 URL을 반환한다")
    void upload_success() throws Exception {
        // given
        setBucket("test-bucket");
        MultipartFile file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn("test.png");
        given(file.getContentType()).willReturn("image/png");
        given(file.getBytes()).willReturn(new byte[]{1, 2, 3});
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        // when
        String url = s3FileService.upload("profile", file);

        // then
        assertThat(url).startsWith("https://test-bucket.s3.amazonaws.com/profile/");
        assertThat(url).endsWith("_test.png");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("파일 읽기 실패 시 RuntimeException이 발생한다")
    void upload_ioException() throws Exception {
        // given
        setBucket("test-bucket");
        MultipartFile file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn("test.png");
        given(file.getContentType()).willReturn("image/png");
        given(file.getBytes()).willThrow(new IOException("디스크 오류"));

        // when & then
        assertThatThrownBy(() -> s3FileService.upload("profile", file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 업로드에 실패했습니다.");
    }
}
