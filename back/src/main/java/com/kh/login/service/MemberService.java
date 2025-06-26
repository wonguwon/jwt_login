package com.kh.login.service;

import com.kh.login.domain.Member;
import com.kh.login.dto.member.MemberCreateDto;
import com.kh.login.dto.member.MemberLoginDto;
import com.kh.login.dto.member.MemberResponseDto;
import com.kh.login.enums.SocialType;

public interface MemberService {
    Member create(MemberCreateDto memberCreateDto);
    Member login(MemberLoginDto memberLoginDto);
    MemberResponseDto getMemberInfoByEmail(String email);

    Member getMemberBySocialId(String socialId);
    Member createOauth(String socialId, String email, String name, SocialType socialType);
}
