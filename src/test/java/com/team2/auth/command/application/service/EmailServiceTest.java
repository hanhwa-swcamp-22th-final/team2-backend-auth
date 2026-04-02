package com.team2.auth.command.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("임시 비밀번호 이메일을 올바른 내용으로 전송한다")
    void sendTemporaryPassword_success() {
        // given
        String toEmail = "user@test.com";
        String userName = "홍길동";
        String tempPassword = "TempPw123!";

        // when
        emailService.sendTemporaryPassword(toEmail, userName, tempPassword);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getFrom()).isEqualTo("noreply@team2.com");
        assertThat(sent.getTo()).containsExactly(toEmail);
        assertThat(sent.getSubject()).isEqualTo("[Team2] 비밀번호가 초기화되었습니다");
        assertThat(sent.getText()).contains(userName);
        assertThat(sent.getText()).contains(tempPassword);
    }
}
