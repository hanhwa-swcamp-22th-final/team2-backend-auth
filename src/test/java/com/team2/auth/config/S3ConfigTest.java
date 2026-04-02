package com.team2.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class S3ConfigTest {

    private S3Config createConfig(String accessKey, String secretKey, String region) throws Exception {
        S3Config config = new S3Config();
        setField(config, "accessKey", accessKey);
        setField(config, "secretKey", secretKey);
        setField(config, "region", region);
        return config;
    }

    private void setField(Object target, String fieldName, String value) throws Exception {
        Field field = S3Config.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("자격증명이 비어있으면 기본 credential chain으로 S3Client를 생성한다")
    void s3Client_withBlankCredentials() throws Exception {
        // given
        S3Config config = createConfig("", "", "ap-northeast-2");

        // when
        S3Client client = config.s3Client();

        // then
        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    @DisplayName("자격증명이 설정되어 있으면 StaticCredentialsProvider로 S3Client를 생성한다")
    void s3Client_withCredentials() throws Exception {
        // given
        S3Config config = createConfig("testAccessKey", "testSecretKey", "ap-northeast-2");

        // when
        S3Client client = config.s3Client();

        // then
        assertThat(client).isNotNull();
        client.close();
    }
}
