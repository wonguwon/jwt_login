package com.kh.login.controller;

import com.kh.login.auth.JwtTokenProvider;
import com.kh.login.domain.Member;
import com.kh.login.dto.member.AccessTokenDto;
import com.kh.login.dto.member.KakaoProfileDto;
import com.kh.login.dto.member.MemberCreateDto;
import com.kh.login.dto.member.MemberLoginDto;
import com.kh.login.dto.member.MemberResponseDto;
import com.kh.login.dto.member.RedirectDto;
import com.kh.login.enums.SocialType;
import com.kh.login.repository.MemberRepository;
import com.kh.login.service.KakaoService;
import com.kh.login.service.MemberService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoService kakaoService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody MemberCreateDto memberCreateDto) {
        Member member = memberService.create(memberCreateDto);
        return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> doLogin(@Valid @RequestBody MemberLoginDto memberLoginDto){
        //email, password 일치한지 검증
        Member member = memberService.login(memberLoginDto);

        //일치할 경우 jwt accesstoken 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("name", member.getName());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponseDto> getMyInfo() {
        // JWT 토큰에서 이메일 추출하여 사용자 정보 조회
        String email = jwtTokenProvider.getUserEmailFromToken();
        MemberResponseDto memberInfo = memberService.getMemberInfoByEmail(email);
        return new ResponseEntity<>(memberInfo, HttpStatus.OK);
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto){
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
        KakaoProfileDto kakaoProfileDto  = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());
        Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getId(), SocialType.KAKAO);
        if(originalMember == null){
            originalMember = memberService.createOauth(
                kakaoProfileDto.getId(), 
                kakaoProfileDto.getKakao_account().getEmail(), 
                kakaoProfileDto.getKakao_account().getProfile().getNickname(),  // nickname을 name으로 사용
                SocialType.KAKAO
            );
        }
        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalMember.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<?> memberList(){
        List<MemberResponseDto> dtos = memberService.findAll();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
}