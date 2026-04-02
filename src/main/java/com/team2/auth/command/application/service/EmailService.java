package com.team2.auth.command.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // TODO: 공용 구글 계정 생성 후 발신 주소 변경
    private static final String FROM_ADDRESS = "noreply@team2.com";

    public void sendTemporaryPassword(String toEmail, String userName, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_ADDRESS);
        message.setTo(toEmail);
        message.setSubject("[Team2] 비밀번호가 초기화되었습니다");
        message.setText(
                userName + "님, 안녕하세요.\n\n"
                + "관리자에 의해 비밀번호가 초기화되었습니다.\n\n"
                + "임시 비밀번호: " + tempPassword + "\n\n"
                + "로그인 후 반드시 비밀번호를 변경해주세요."
        );
        mailSender.send(message);
    }
}
