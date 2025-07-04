package com.kh.login.service;

import com.kh.login.dto.mail.MailRequestDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendMail(MailRequestDto request, MultipartFile file) throws MessagingException {
        // 1. 템플릿에 사용할 데이터 구성
        Context context = new Context();
        context.setVariable("title", request.getTitle());
        context.setVariable("body", request.getBody());

        boolean isFile = file != null && !file.isEmpty();

        // 2. 템플릿 렌더링
        String htmlContent = templateEngine.process("email-template", context);

        // 3. 메일 생성 및 전송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, isFile, "UTF-8");

        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(htmlContent, true);
        helper.setFrom("wldnjsv1004@gmail.com");

        if(isFile) {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            helper.addAttachment(originalFilename, file);
        }

        mailSender.send(message);
    }
}
