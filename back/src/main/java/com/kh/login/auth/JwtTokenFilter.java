package com.kh.login.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component  // Spring에서 이 클래스를 Bean으로 등록
public class JwtTokenFilter extends GenericFilter {

    @Value("${jwt.secret}")  // application.yml에 설정된 JWT 시크릿 키 주입
    private String secretKey;
    
    private final Key SECRET_KEY;

    public JwtTokenFilter(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
        this.SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰 검증 필터
     * 모든 요청에 대해 실행되며, Authorization 헤더에 포함된 JWT를 검증함
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // 요청 헤더에서 Authorization 값을 가져옴
        String token = httpServletRequest.getHeader("Authorization");

        try {
            if (token != null) {
                // Bearer 형식이 아닌 경우 예외 발생
                if (!token.substring(0, 7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식 아닙니다.");
                }

                // "Bearer " 이후 실제 JWT 문자열 추출
                String jwtToken = token.substring(7);

                // JWT 파싱 및 서명 검증 → payload 추출
                Claims claims = Jwts.parserBuilder()
                                    .setSigningKey(SECRET_KEY)  // Key 객체 사용
                                    .build()
                                    .parseClaimsJws(jwtToken)
                                    .getBody();  // payload (claims) 반환

                // 사용자 권한 정보를 Spring Security 형식으로 변환
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));

                // JWT의 subject 값을 username으로 사용
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
                // 인증 객체 생성 (세션은 사용하지 않음)
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, jwtToken, userDetails.getAuthorities());
                // 현재 요청에 대해 인증 정보 등록
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 다음 필터로 요청을 넘김
            chain.doFilter(request, response);

        } catch (Exception e) {
            e.printStackTrace();

            // 예외 발생 시 401 Unauthorized 응답 반환
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("invalid token");
        }
    }
}
