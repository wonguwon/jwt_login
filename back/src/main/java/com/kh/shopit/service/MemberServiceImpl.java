package com.kh.shopit.service;

import com.kh.shopit.auth.JwtTokenProvider;
import com.kh.shopit.domain.Member;
import com.kh.shopit.dto.member.MemberCreateDto;
import com.kh.shopit.dto.member.MemberLoginDto;
import com.kh.shopit.dto.member.MemberResponseDto;
import com.kh.shopit.exception.InvalidCredentialsException;
import com.kh.shopit.exception.UserAlreadyExistsException;
import com.kh.shopit.exception.UserNotFoundException;
import com.kh.shopit.repository.MemberRepository;
import java.util.Optional;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member create(MemberCreateDto memberCreateDto) {
        // 이메일 중복 검증
        if (memberRepository.existsByEmail(memberCreateDto.getEmail())) {
            throw new UserAlreadyExistsException("이미 존재하는 이메일입니다.");
        }
        
        // 전화번호 중복 검증
        if (memberRepository.existsByPhoneNumber(memberCreateDto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("이미 존재하는 전화번호입니다.");
        }

        Member member = Member.builder()
                              .name(memberCreateDto.getName())
                              .email(memberCreateDto.getEmail())
                              .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                              .phoneNumber(memberCreateDto.getPhoneNumber())
                              .build();
        memberRepository.save(member);
        return member;
    }

    public Member login(MemberLoginDto memberLoginDto) {
        Optional<Member> optMember = memberRepository.findByEmail(memberLoginDto.getEmail());
        if(!optMember.isPresent()){
            throw new InvalidCredentialsException("이메일이 존재하지 않습니다.");
        }

        Member member = optMember.get();
        if(!passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())){
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    @Override
    public MemberResponseDto getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));
        return MemberResponseDto.from(member);
    }
}
