package com.kh.shopit.service;

import com.kh.shopit.auth.JwtTokenProvider;
import com.kh.shopit.domain.Member;
import com.kh.shopit.dto.member.MemberCreateDto;
import com.kh.shopit.dto.member.MemberLoginDto;
import com.kh.shopit.dto.member.MemberResponseDto;

public interface MemberService {
    Member create(MemberCreateDto memberCreateDto);
    Member login(MemberLoginDto memberLoginDto);
    MemberResponseDto getMemberInfo(Long memberId);
}
