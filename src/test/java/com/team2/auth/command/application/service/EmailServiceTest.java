package com.team2.auth.command.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String FROM_ADDRESS = "team2-noreply@example.com";

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void injectFromAddress() {
        // EmailService 는 @Value("${spring.mail.username:}") 로 From 주소를 주입받는다.
        // 단위 테스트에서는 Spring Context 를 띄우지 않으므로 ReflectionTestUtils 로 주입한다.
        ReflectionTestUtils.setField(emailService, "fromAddress", FROM_ADDRESS);
    }

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
        assertThat(sent.getFrom()).isEqualTo(FROM_ADDRESS);
        assertThat(sent.getTo()).containsExactly(toEmail);
        assertThat(sent.getSubject()).isEqualTo("[Team2] 비밀번호가 초기화되었습니다");
        assertThat(sent.getText()).contains(userName);
        assertThat(sent.getText()).contains(tempPassword);
    }

    @Test
    @DisplayName("spring.mail.username 이 비어있으면 IllegalStateException 을 던진다")
    void sendTemporaryPassword_throwsWhenFromAddressMissing() {
        // given: fromAddress 를 blank 로 덮어쓴다
        ReflectionTestUtils.setField(emailService, "fromAddress", "");

        // when / then
        assertThatThrownBy(() -> emailService.sendTemporaryPassword("u@t.com", "u", "pw"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("메일 발신자가 설정되지 않았습니다");
    }
}
