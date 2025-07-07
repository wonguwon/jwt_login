package com.kh.login.service;

import com.kh.login.domain.Member;
import com.kh.login.dto.member.MemberCreateDto;
import com.kh.login.dto.member.MemberLoginDto;
import com.kh.login.dto.member.MemberResponseDto;
import com.kh.login.enums.SocialType;
import com.kh.login.exception.InvalidCredentialsException;
import com.kh.login.exception.UserAlreadyExistsException;
import com.kh.login.exception.UserNotFoundException;
import com.kh.login.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    public MemberResponseDto getMemberInfoByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));
        return MemberResponseDto.from(member);
    }

    @Override
    public Member getMemberBySocialId(String socialId, SocialType socialType){
        Member member = memberRepository.findBySocialIdAndSocialType(socialId, socialType).orElse(null);
        return member;
    }

    @Override
    public Member createOauth(String socialId, String email, String name, SocialType socialType){
        Member member = Member.builder()
                .email(email)
                .name(name)
                .password("")
                .phoneNumber(null)
                .socialType(socialType)
                .socialId(socialId)
                .build();
        memberRepository.save(member);
        return member;
    }

    @Override
    public List<MemberResponseDto> findAll() {
        List<Member> members = memberRepository.findAll();

        List<MemberResponseDto> memberListResDtos = members.stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());

        return memberListResDtos;
    }
}
