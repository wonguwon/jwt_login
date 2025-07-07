package com.kh.login.config;

import com.kh.login.auth.JwtTokenFilter;
import com.kh.login.service.GoogleOauth2LoginSuccess;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final GoogleOauth2LoginSuccess googleOauth2LoginSuccess;  // OAuth 로그인 성공 핸들러 주입

    public SecurityConfig(JwtTokenFilter jwtTokenFilter, GoogleOauth2LoginSuccess googleOauth2LoginSuccess) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.googleOauth2LoginSuccess = googleOauth2LoginSuccess;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(configurationSource()))
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 기능 비활성화 (REST API 서버에서는 필요 없음, 세션을 통한 공격)
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화(아이디와 비밀번호를 HTTP 요청 헤더에 실어서 인증하는 방식)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //// 세션 사용 안 함 (JWT를 사용하는 stateless 구조)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/member/login",    //일반로그인
                                "/v1/member/signup",    //일반 회원가입
                                "/v1/member/kakao/login",    // 카카오 로그인 (수동 구현)
                                "/auth/email/*",
                                "/oauth2/**",                // Spring OAuth2 클라이언트 경로
                                "/connect/**"
                        ).permitAll()
                        .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
                )
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // OAuth2 로그인 성공 시 실행될 핸들러 설정 (회원 정보 등록 + JWT 발급 등)
                .oauth2Login(o -> o.successHandler(googleOauth2LoginSuccess))
                .build();
    }

    /**
     * 비밀번호 인코더 빈 설정
     * Spring Security에서 지원하는 다양한 인코딩 방식을 위임하여 사용하는 Encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * CORS 설정 정의: React 개발 서버(3000번 포트)에서 오는 요청을 허용
     */
    @Bean
    public CorsConfigurationSource configurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        // 요청을 허용할 도메인(origin)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등)
        configuration.setAllowedMethods(Arrays.asList("*"));
        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 자격 증명 (쿠키, 인증 헤더 등) 허용
        configuration.setAllowCredentials(true);
        // 위 설정을 모든 URL 패턴에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}