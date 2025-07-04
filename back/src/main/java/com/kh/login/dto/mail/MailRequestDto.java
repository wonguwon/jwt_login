package com.kh.login.dto.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class MailRequestDto {
    private String to;
    private String subject;
    private String title;
    private String body;
}
