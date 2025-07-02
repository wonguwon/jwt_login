package com.kh.login.service;


import com.kh.login.auth.JwtTokenProvider;
import com.kh.login.domain.Member;
import com.kh.login.enums.SocialType;
import com.kh.login.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

@Service
public class GoogleOauth2LoginSuccess extends SimpleUrlAuthenticationSuccessHandler {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public GoogleOauth2LoginSuccess(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        //Spring Security가 OAuth2 로그인 과정에서 만들어주는 사용자 정보 객체
        //내부에 Google이 응답한 사용자 JSON 정보가 key-value 형식으로 담김
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String openId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");

        //회원가입 여부 확인
        Member member = memberRepository.findBySocialIdAndSocialType(openId, SocialType.GOOGLE).orElse(null);
        if(member == null){
            member = Member.builder()
                    .socialId(openId)
                    .email(email)
                    .name(oAuth2User.getAttribute("name") != null ? oAuth2User.getAttribute("name") : "Google User")
                    .password("") // OAuth 사용자는 비밀번호 없음
                    .phoneNumber(null) // OAuth 사용자는 전화번호 없음
                    .socialType(SocialType.GOOGLE)
                    .build();
            memberRepository.save(member);
        }
//        jwt토큰 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());

//        클라이언트 redirect 방식으로 토큰 전달
//        response.sendRedirect("http://localhost:3000?token="+jwtToken);

        Cookie jwtCookie = new Cookie("token", jwtToken);
        jwtCookie.setPath("/"); //모든 경로에서 쿠키 사용가능
        response.addCookie(jwtCookie);
        response.sendRedirect("http://localhost:3000");

    }

}
