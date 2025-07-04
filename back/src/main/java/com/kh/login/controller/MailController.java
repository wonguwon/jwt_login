package com.kh.login.controller;

import com.kh.login.dto.mail.MailRequestDto;
import com.kh.login.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    //multipart/form-data 요청의 각 "부분(part)"을 개별적으로 받을 때
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendMail(
            @RequestPart("mail") MailRequestDto request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            mailService.sendMail(request, file);
            return ResponseEntity.ok("HTML 메일 + 첨부 전송 성공!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("메일 전송 실패: " + e.getMessage());
        }
    }
}
