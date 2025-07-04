package com.kh.login.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerifyRequestDto {
    private String email;
    private String code;
} 