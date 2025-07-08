package com.kh.login.service;

import com.kh.login.domain.EmailVerification;
import com.kh.login.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationRepository repository;
    private final JavaMailSender mailSender;

    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));

        EmailVerification verification = new EmailVerification();
        verification.setData(email, code, LocalDateTime.now(), false);
        repository.save(verification);

        // 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증코드");
        message.setText("인증코드: " + code);
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        Optional<EmailVerification> optional = repository.findTopByEmailOrderByCreatedAtDesc(email);
        if (optional.isEmpty()) return false;
        EmailVerification verification = optional.get();

        // 3분(180초) 이내, 미인증, 코드 일치
        if (!verification.isVerified()
            && verification.getCode().equals(code)
            && verification.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(3))) {
            verification.changeVerified(true);
            repository.save(verification);
            return true;
        }
        return false;
    }
} 