# JWT & OAuth2 로그인 시스템 완벽 가이드 🚀

## 📋 목차
1. [기본 개념 이해](#1-기본-개념-이해)
2. [회원가입 과정](#2-회원가입-과정)
3. [일반 로그인 과정](#3-일반-로그인-과정)
4. [JWT 토큰 이해](#4-jwt-토큰-이해)
5. [OAuth2 소셜 로그인 과정](#5-oauth2-소셜-로그인-과정)
6. [실제 구현 흐름](#6-실제-구현-흐름)

---

## 1. 기본 개념 이해

### 🔐 JWT (JSON Web Token)란?
- **정의**: 사용자 정보를 담은 디지털 서명된 토큰
- **구조**: `헤더.페이로드.서명` (3부분으로 구성)
- **용도**: 로그인 상태를 유지하는 방법

### 🌐 OAuth2란?
- **정의**: 제3자 서비스(Google, Kakao 등)를 통한 로그인 방식
- **장점**: 비밀번호 없이 안전하게 로그인 가능
- **예시**: "Google로 로그인", "Kakao로 로그인"

---

## 2. 회원가입 과정

### 📝 단계별 흐름

```
1. 사용자가 회원가입 폼 작성
   ↓
2. React에서 백엔드로 데이터 전송
   ↓
3. 백엔드에서 비밀번호 암호화
   ↓
4. 데이터베이스에 저장
   ↓
5. 성공 응답 반환
```

### 🔍 상세 설명

#### 2-1. 프론트엔드 (React)
```javascript
// 사용자가 입력한 정보
const userData = {
  email: "user@example.com",
  password: "mypassword123"
}

// 백엔드로 전송
axios.post("http://localhost:8080/member/create", userData)
```

#### 2-2. 백엔드 (Spring Boot)
```java
// 1. 비밀번호 암호화
String encodedPassword = passwordEncoder.encode(password);

// 2. 사용자 정보 저장
Member member = new Member(email, encodedPassword);
memberRepository.save(member);
```

### 💡 핵심 포인트
- **비밀번호는 절대 평문으로 저장하지 않음**
- **암호화된 비밀번호는 복호화 불가능**
- **이메일 중복 체크 필요**

---

## 3. 일반 로그인 과정

### 📝 단계별 흐름

```
1. 사용자가 이메일/비밀번호 입력
   ↓
2. 백엔드에서 사용자 정보 확인
   ↓
3. 비밀번호 일치 여부 검증
   ↓
4. JWT 토큰 생성
   ↓
5. 토큰을 프론트엔드로 반환
   ↓
6. 프론트엔드에서 토큰 저장
```

### 🔍 상세 설명

#### 3-1. 프론트엔드 (React)
```javascript
const loginData = {
  email: "user@example.com",
  password: "mypassword123"
}

// 로그인 요청
const response = await axios.post("http://localhost:8080/member/doLogin", loginData);

// JWT 토큰 저장
localStorage.setItem("token", response.data.token);
```

#### 3-2. 백엔드 (Spring Boot)
```java
// 1. 사용자 정보 조회
Member member = memberRepository.findByEmail(email);

// 2. 비밀번호 검증
if (passwordEncoder.matches(password, member.getPassword())) {
    // 3. JWT 토큰 생성
    String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole());
    return token;
}
```

### 💡 핵심 포인트
- **비밀번호는 암호화된 상태로 비교**
- **JWT 토큰에는 사용자 정보가 포함됨**
- **토큰은 클라이언트에 저장되어 인증에 사용**

---

## 4. JWT 토큰 이해

### 🔐 JWT 구조

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

#### 4-1. 헤더 (Header)
```json
{
  "alg": "HS256",  // 암호화 알고리즘
  "typ": "JWT"     // 토큰 타입
}
```

#### 4-2. 페이로드 (Payload)
```json
{
  "sub": "user@example.com",  // 사용자 식별자
  "role": "USER",             // 사용자 권한
  "iat": 1516239022,          // 발급 시간
  "exp": 1516242622           // 만료 시간
}
```

#### 4-3. 서명 (Signature)
- 헤더와 페이로드를 비밀키로 암호화한 값
- 토큰의 무결성을 보장

### 🔍 JWT 사용 과정

```
1. 로그인 성공 시 JWT 토큰 발급
   ↓
2. 클라이언트에서 토큰 저장 (localStorage)
   ↓
3. API 요청 시 헤더에 토큰 포함
   ↓
4. 서버에서 토큰 검증
   ↓
5. 유효한 토큰이면 요청 처리
```

### 💡 핵심 포인트
- **JWT는 서버에 저장되지 않음 (Stateless)**
- **토큰 자체에 사용자 정보가 포함됨**
- **만료 시간이 있어 보안성 확보**

---

## 5. OAuth2 소셜 로그인 과정

### 📝 OAuth2 흐름 (Authorization Code 방식)

```
1. 사용자가 "Google로 로그인" 클릭
   ↓
2. Google 로그인 페이지로 리다이렉트
   ↓
3. 사용자가 Google에서 로그인
   ↓
4. Google에서 인증 코드를 우리 서비스로 전송
   ↓
5. 백엔드에서 인증 코드로 액세스 토큰 요청
   ↓
6. 액세스 토큰으로 사용자 정보 요청
   ↓
7. 사용자 정보로 JWT 토큰 생성
   ↓
8. 프론트엔드로 JWT 토큰 반환
```

### 🔍 상세 설명

#### 5-1. Google OAuth2 설정
```yaml
# application.yml
oauth:
  google:
    client-id: "your-google-client-id"
    client-secret: "your-google-client-secret"
    redirect-uri: "http://localhost:3000/oauth/google/redirect"
```

#### 5-2. 프론트엔드 (React)
```javascript
// Google 로그인 버튼 클릭
const googleServerLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
}

// 또는 직접 Google OAuth URL 사용
const googleLogin = () => {
  const authUrl = `https://accounts.google.com/o/oauth2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=openid email profile`;
  window.location.href = authUrl;
}
```

#### 5-3. 백엔드 (Spring Boot)
```java
@PostMapping("/google/doLogin")
public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto) {
    // 1. 인증 코드로 액세스 토큰 요청
    AccessTokenDto accessToken = googleService.getAccessToken(redirectDto.getCode());
    
    // 2. 액세스 토큰으로 사용자 정보 요청
    GoogleProfileDto profile = googleService.getGoogleProfile(accessToken.getAccess_token());
    
    // 3. 사용자 정보로 회원가입 또는 로그인
    Member member = memberService.getMemberBySocialId(profile.getSub());
    if (member == null) {
        member = memberService.createOauth(profile.getSub(), profile.getEmail(), SocialType.GOOGLE);
    }
    
    // 4. JWT 토큰 생성
    String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole());
    
    return ResponseEntity.ok(Map.of("token", jwtToken));
}
```

### 💡 핵심 포인트
- **OAuth2는 비밀번호 없이 안전한 로그인**
- **인증 코드는 일회성이며 보안성 높음**
- **소셜 로그인도 최종적으로는 JWT 토큰 사용**

---

## 6. 실제 구현 흐름

### 🎯 전체 시스템 아키텍처

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   React     │    │ Spring Boot │    │  Database   │
│ (Frontend)  │◄──►│ (Backend)   │◄──►│ (MySQL)     │
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       │                   │                   │
       ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ LocalStorage│    │ JWT Token   │    │ User Table  │
│ (Token)     │    │ Generation  │    │ (Encrypted) │
└─────────────┘    └─────────────┘    └─────────────┘
```

### 🔄 API 호출 흐름

#### 6-1. 회원가입 API
```
POST /member/create
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### 6-2. 로그인 API
```
POST /member/doLogin
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "id": 1,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 6-3. 소셜 로그인 API
```
POST /member/google/doLogin
Content-Type: application/json

{
  "code": "4/0AfJohXn..."
}

Response:
{
  "id": 1,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 🛡️ 보안 고려사항

#### 6-1. CORS 설정
```java
@Bean
public CorsConfigurationSource configurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    return source;
}
```

#### 6-2. JWT 필터
```java
@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) {
        // JWT 토큰 검증 로직
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 인증 성공
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## 🎓 학습 체크리스트

### ✅ 기본 개념
- [ ] JWT의 구조와 역할 이해
- [ ] OAuth2의 흐름 이해
- [ ] Stateless vs Stateful 인증의 차이

### ✅ 구현 과정
- [ ] 회원가입 API 구현
- [ ] 로그인 API 구현
- [ ] JWT 토큰 생성 및 검증
- [ ] OAuth2 소셜 로그인 구현
- [ ] CORS 설정 이해

### ✅ 보안 고려사항
- [ ] 비밀번호 암호화
- [ ] JWT 토큰 만료 시간 설정
- [ ] CORS 정책 설정
- [ ] HTTPS 사용 권장

---

## 🚀 다음 단계

1. **실제 프로젝트 실행**
   - 백엔드 서버 실행: `./gradlew bootRun`
   - 프론트엔드 서버 실행: `npm run dev`

2. **테스트 진행**
   - 회원가입 테스트
   - 로그인 테스트
   - 소셜 로그인 테스트

3. **고급 기능 추가**
   - 토큰 갱신 (Refresh Token)
   - 로그아웃 기능
   - 권한 관리 (Role-based Access Control)

---

## 📚 참고 자료

- [JWT 공식 문서](https://jwt.io/)
- [OAuth2 공식 문서](https://oauth.net/2/)
- [Spring Security 문서](https://spring.io/projects/spring-security)
- [React Router 문서](https://reactrouter.com/)

---

**🎉 축하합니다! 이제 JWT와 OAuth2 로그인 시스템의 전체적인 흐름을 이해하셨습니다.** 