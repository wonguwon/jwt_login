# OAuth 로그인 구현 방식 정리

## 목차
1. [개요](#개요)
2. [Spring Security OAuth2 방식 (Google 예시)](#spring-security-oauth2-방식-google-예시)
3. [수동 구현 방식 (Kakao 예시)](#수동-구현-방식-kakao-예시)
4. [두 방식의 비교](#두-방식의-비교)
5. [실제 구현 예시](#실제-구현-예시)
6. [정리 및 권장사항](#정리-및-권장사항)

---

## 개요

OAuth 로그인을 구현하는 방법은 크게 두 가지가 있습니다:
1. **Spring Security OAuth2 방식** (권장) - Google 예시
2. **수동 구현 방식** - Kakao 예시

각 방식의 특징과 흐름을 상세히 설명합니다.

---

## Spring Security OAuth2 방식 (Google 예시)

### 특징
- Spring Security가 OAuth2 인증 과정을 자동으로 처리
- 프론트엔드에서 별도의 OAuth 처리 로직 불필요
- 보안성이 높고 구현이 간단함

### 전체 흐름
```
1. 사용자 클릭 → 2. Spring OAuth2 엔드포인트 → 3. Google 인증 → 4. 백엔드에서 직접 처리 → 5. JWT 토큰 발급
```

### 상세 흐름

#### 1. 프론트엔드 (React)
```javascript
// MemberLogin.jsx
const googleServerLogin = () => {
  window.location.href = "http://localhost:8001/oauth2/authorization/google"
}

// Google 로그인 버튼 클릭 시
<SocialImage
  src={googleLoginImg}
  alt="Google Login"
  onClick={googleServerLogin}  // 이 함수 호출
/>
```

#### 2. Spring Security OAuth2 처리
```yaml
# application.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: "your-google-client-id"
            client-secret: "your-google-client-secret"
            scope: openid, email, profile
            redirect-uri: "http://localhost:8001/login/oauth2/code/google"
```

#### 3. Google 인증 과정
1. 사용자가 `http://localhost:8001/oauth2/authorization/google` 접속
2. Spring Security가 Google OAuth URL로 리다이렉트
3. 사용자가 Google에서 로그인
4. Google이 인증 코드를 백엔드로 직접 전송
5. Spring Security가 자동으로 액세스 토큰 요청 및 사용자 정보 조회

#### 4. 로그인 성공 처리
```java
// GoogleOauth2LoginSuccess.java
@Service
public class GoogleOauth2LoginSuccess extends SimpleUrlAuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) {
        // 1. OAuth2 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String openId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        
        // 2. 회원가입 여부 확인 및 처리
        Member member = memberRepository.findBySocialId(openId).orElse(null);
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
        
        // 3. JWT 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        
        // 4. 프론트엔드로 리다이렉트
        Cookie jwtCookie = new Cookie("token", jwtToken);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);
        response.sendRedirect("http://localhost:3000");
    }
}
```

#### 5. 프론트엔드에서 토큰 처리
```javascript
// HeaderComponent.jsx
useEffect(() => {
  const token = Cookies.get("token")
  if (token) {
    sessionStorage.setItem("token", token)
    Cookies.remove("token")
    setIsLogin(true)
    fetchUserInfo()
  }
}, [])

const fetchUserInfo = async () => {
  try {
    const memberInfo = await getMyInfo()  // JWT 토큰 사용
    setUserName(memberInfo.name)
  } catch (error) {
    console.error('사용자 정보 조회 실패:', error)
  }
}
```

---

## 수동 구현 방식 (Kakao 예시)

### 특징
- 프론트엔드에서 직접 Kakao OAuth URL 호출
- 인증 코드를 받아서 백엔드로 전송
- 더 많은 제어 가능하지만 구현이 복잡함

### 전체 흐름
```
1. 사용자 클릭 → 2. Kakao OAuth URL → 3. Kakao 인증 → 4. 프론트엔드로 코드 전달 → 5. 백엔드로 코드 전송 → 6. JWT 토큰 발급
```

### 상세 흐름

#### 1. 프론트엔드 (React)
```javascript
// MemberLogin.jsx
const kakaoUrl = "https://kauth.kakao.com/oauth/authorize"
const kakaoClientId = "your-kakao-javascript-key"  // JavaScript 키 사용
const kakaoRedirectUrl = "http://localhost:3000/oauth/kakao/redirect"
const kakaoScope = "profile_nickname profile_image account_email"

const kakaoLogin = () => {
  const auth_uri = `${kakaoUrl}?client_id=${kakaoClientId}&redirect_uri=${kakaoRedirectUrl}&response_type=code&scope=${kakaoScope}`
  window.location.href = auth_uri
}

// Kakao 로그인 버튼 클릭 시
<SocialImage
  src={kakaoLoginImg}
  alt="Kakao Login"
  onClick={kakaoLogin}  // 이 함수 호출
/>
```

#### 2. Kakao 인증 과정
1. 사용자가 Kakao OAuth URL로 리다이렉트
2. Kakao에서 로그인 및 권한 승인
3. Kakao가 인증 코드를 `kakaoRedirectUrl`로 전송

#### 3. 프론트엔드에서 코드 처리
```javascript
// KakaoRedirect.jsx
const KakaoRedirect = () => {
  useEffect(() => {
    const code = new URL(window.location.href).searchParams.get("code")
    sendCodeToServer(code)
  }, [])

  const sendCodeToServer = async (code) => {
    try {
      const response = await axios.post("http://localhost:8001/member/kakao/login", { code })
      const token = response.data.token
      sessionStorage.setItem("token", token)
      window.location.href = "/"
    } catch (error) {
      console.error("Kakao 로그인 실패:", error)
      alert("Kakao 로그인에 실패했습니다.")
    }
  }
}
```

#### 4. 백엔드에서 코드 처리
```java
// MemberController.java
@PostMapping("/kakao/login")
public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto){
    // 1. 인증 코드로 액세스 토큰 요청
    AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
    
    // 2. 액세스 토큰으로 사용자 정보 요청
    KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());
    
    // 3. 회원가입 여부 확인 및 처리
    Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getId().toString());
    if(originalMember == null){
        originalMember = memberService.createOauth(kakaoProfileDto.getId().toString(), 
                                                  kakaoProfileDto.getKakao_account().getEmail(), 
                                                  kakaoProfileDto.getProperties().getNickname(),
                                                  SocialType.KAKAO);
    }
    
    // 4. JWT 토큰 발급
    String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), 
                                                  originalMember.getRole().toString());

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("id", originalMember.getId());
    loginInfo.put("token", jwtToken);
    return new ResponseEntity<>(loginInfo, HttpStatus.OK);
}
```

#### 5. KakaoService 구현
```java
// KakaoService.java
@Service
public class KakaoService {
    
    public AccessTokenDto getAccessToken(String code) {
        // Kakao OAuth2 토큰 엔드포인트로 POST 요청
        // client_id, client_secret, code, redirect_uri, grant_type 전송
        // 액세스 토큰 응답 받기
    }
    
    public KakaoProfileDto getKakaoProfile(String accessToken) {
        // Kakao User Info 엔드포인트로 GET 요청
        // Authorization 헤더에 Bearer 토큰 포함
        // 사용자 정보 응답 받기
    }
}
```

---

## 두 방식의 비교

| 구분 | Spring Security OAuth2 | 수동 구현 방식 |
|------|----------------------|---------------|
| **구현 복잡도** | 낮음 | 높음 |
| **보안성** | 높음 (Spring Security가 처리) | 중간 (직접 구현) |
| **유지보수** | 쉬움 | 어려움 |
| **제어 가능성** | 제한적 | 높음 |
| **추가 라이브러리** | spring-boot-starter-oauth2-client | 별도 없음 |
| **에러 처리** | 자동화됨 | 수동 구현 필요 |
| **토큰 갱신** | 자동 처리 | 수동 구현 필요 |

---

## 실제 구현 예시

### 현재 프로젝트에서 사용 중인 방식

#### Google OAuth (Spring Security OAuth2 방식)
- `googleServerLogin()` 함수
- `GoogleOauth2LoginSuccess` 클래스
- `application.yml`의 OAuth2 설정

#### Kakao OAuth (수동 구현 방식)
- `kakaoLogin()` 함수
- `KakaoRedirect.jsx` 컴포넌트
- `/member/kakao/login` 엔드포인트
- `KakaoService`, `KakaoProfileDto`, `AccessTokenDto`, `RedirectDto` 클래스들

---

## 정리 및 권장사항

### 1. 권장 방식
**Spring Security OAuth2 방식**을 권장합니다.
- 구현이 간단하고 안전함
- Spring Security의 검증된 보안 기능 활용
- 유지보수가 용이함

### 2. 현재 프로젝트 구조
현재 프로젝트에서는 두 가지 방식을 모두 사용하고 있습니다:

#### Google OAuth (Spring Security OAuth2 방식)
```
프론트엔드: googleServerLogin() → Spring OAuth2 엔드포인트
백엔드: GoogleOauth2LoginSuccess → JWT 토큰 발급
프론트엔드: 쿠키에서 토큰 추출 → 사용자 정보 조회
```

#### Kakao OAuth (수동 구현 방식)
```
프론트엔드: kakaoLogin() → Kakao OAuth URL → KakaoRedirect.jsx
백엔드: /member/kakao/login → KakaoService → JWT 토큰 발급
프론트엔드: 응답에서 토큰 추출 → 사용자 정보 조회
```

### 3. 앱키 설정
#### Google OAuth
- **Client ID**: OAuth 2.0 클라이언트 ID
- **Client Secret**: OAuth 2.0 클라이언트 시크릿
- **Redirect URI**: `http://localhost:8001/login/oauth2/code/google`

#### Kakao OAuth
- **JavaScript 키**: 프론트엔드에서 사용
- **REST API 키**: 백엔드에서 사용
- **Redirect URI**: `http://localhost:3000/oauth/kakao/redirect`

### 4. 최종 권장사항
1. **Google OAuth**: 현재 Spring Security OAuth2 방식 유지 (권장)
2. **Kakao OAuth**: 현재 수동 구현 방식 유지 (Kakao는 Spring Security OAuth2 지원이 제한적)
3. **일관성**: 가능하면 Spring Security OAuth2 방식으로 통일하는 것을 권장

이렇게 정리하면 각 OAuth 제공자의 특성에 맞는 최적의 구현 방식을 사용할 수 있습니다. 