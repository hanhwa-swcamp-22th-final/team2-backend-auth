package com.team2.auth.command.application.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    /**
     * Gmail SMTP 정책상 From 주소는 반드시 인증 계정과 동일해야 한다.
     * 하드코딩된 noreply@team2.com 을 쓰면 메일이 거부되거나 강제 치환된다.
     * docker-compose / k8s secret 의 MAIL_USERNAME 을 그대로 발신자로 사용한다.
     */
    @Value("${spring.mail.username:}")
    private String fromAddress;

    public void sendTemporaryPassword(String toEmail, String userName, String tempPassword) {
        if (fromAddress == null || fromAddress.isBlank()) {
            log.error("spring.mail.username 미설정 — 비밀번호 초기화 메일을 발송할 수 없습니다.");
            throw new IllegalStateException("메일 발신자가 설정되지 않았습니다.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("[Team2] 비밀번호가 초기화되었습니다");
        message.setText(
                userName + "님, 안녕하세요.\n\n"
                + "관리자에 의해 비밀번호가 초기화되었습니다.\n\n"
                + "임시 비밀번호: " + tempPassword + "\n\n"
                + "로그인 후 반드시 비밀번호를 변경해주세요."
        );

        try {
            mailSender.send(message);
            log.info("비밀번호 초기화 메일 발송 성공: to={}", maskEmail(toEmail));
        } catch (Exception e) {
            log.error("비밀번호 초기화 메일 발송 실패: to={}, cause={}", maskEmail(toEmail), e.getMessage());
            throw e;
        }
    }

    /** 로그에서 PII 노출을 줄이기 위해 메일 주소를 마스킹한다. */
    private String maskEmail(String email) {
        if (email == null) return "(null)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
