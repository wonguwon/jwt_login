package com.kh.login.controller;

import com.kh.login.dto.member.EmailVerificationRequestDto;
import com.kh.login.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService service;

    @PostMapping("/send")
    public ResponseEntity<?> sendCode(@RequestBody EmailVerificationRequestDto.Send request) {
        service.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("인증코드가 발송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody EmailVerificationRequestDto.Verify request) {
        boolean result = service.verifyCode(request.getEmail(), request.getCode());
        if (result) {
            return ResponseEntity.ok("인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증코드가 올바르지 않거나 만료되었습니다.");
        }
    }
} 