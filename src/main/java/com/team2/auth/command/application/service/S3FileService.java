package com.team2.auth.command.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;

    // TODO: application.yml에 실제 버킷명 설정 필요
    @Value("${cloud.aws.s3.bucket:team2-bucket}")
    private String bucket;

    public String upload(String directory, MultipartFile file) {
        String key = directory + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }

        // TODO: CloudFront 도메인 또는 S3 퍼블릭 URL로 변경 필요
        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }
}
