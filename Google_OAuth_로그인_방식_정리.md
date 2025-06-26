# Google OAuth 로그인 구현 방식 정리

## 목차
1. [개요](#개요)
2. [Spring Security OAuth2 방식](#spring-security-oauth2-방식)
3. [수동 구현 방식](#수동-구현-방식)
4. [두 방식의 비교](#두-방식의-비교)
5. [실제 구현 예시](#실제-구현-예시)
6. [정리 및 권장사항](#정리-및-권장사항)

---

## 개요

Google OAuth 로그인을 구현하는 방법은 크게 두 가지가 있습니다:
1. **Spring Security OAuth2 방식** (권장)
2. **수동 구현 방식**

각 방식의 특징과 흐름을 상세히 설명합니다.

---

## Spring Security OAuth2 방식

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
                    .name(oAuth2User.getAttribute("name"))
                    .password("")
                    .phoneNumber("")
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

## 수동 구현 방식

### 특징
- 프론트엔드에서 직접 Google OAuth URL 호출
- 인증 코드를 받아서 백엔드로 전송
- 더 많은 제어 가능하지만 구현이 복잡함

### 전체 흐름
```
1. 사용자 클릭 → 2. Google OAuth URL → 3. Google 인증 → 4. 프론트엔드로 코드 전달 → 5. 백엔드로 코드 전송 → 6. JWT 토큰 발급
```

### 상세 흐름

#### 1. 프론트엔드 (React)
```javascript
// MemberLogin.jsx
const googleUrl = "https://accounts.google.com/o/oauth2/auth"
const googleClientId = "your-google-client-id"
const googleRedirectUrl = "http://localhost:3000/oauth/google/redirect"
const googleScope = "openid email profile"

const googleLogin = () => {
  const auth_uri = `${googleUrl}?client_id=${googleClientId}&redirect_uri=${googleRedirectUrl}&response_type=code&scope=${googleScope}`
  window.location.href = auth_uri
}

// Google 로그인 버튼 클릭 시
<SocialImage
  src={googleLoginImg}
  alt="Google Login"
  onClick={googleLogin}  // 이 함수 호출
/>
```

#### 2. Google 인증 과정
1. 사용자가 Google OAuth URL로 리다이렉트
2. Google에서 로그인 및 권한 승인
3. Google이 인증 코드를 `googleRedirectUrl`로 전송

#### 3. 프론트엔드에서 코드 처리
```javascript
// GoogleRedirect.jsx
const GoogleRedirect = () => {
  useEffect(() => {
    const code = new URL(window.location.href).searchParams.get("code")
    sendCodeToServer(code)
  }, [])

  const sendCodeToServer = async (code) => {
    try {
      const response = await axios.post("http://localhost:8001/member/google/login", { code })
      const token = response.data.token
      sessionStorage.setItem("token", token)
      window.location.href = "/"
    } catch (error) {
      console.error("Google 로그인 실패:", error)
      alert("Google 로그인에 실패했습니다.")
    }
  }
}
```

#### 4. 백엔드에서 코드 처리
```java
// MemberController.java
@PostMapping("/google/login")
public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto){
    // 1. 인증 코드로 액세스 토큰 요청
    AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());
    
    // 2. 액세스 토큰으로 사용자 정보 요청
    GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessTokenDto.getAccess_token());
    
    // 3. 회원가입 여부 확인 및 처리
    Member originalMember = memberService.getMemberBySocialId(googleProfileDto.getSub());
    if(originalMember == null){
        originalMember = memberService.createOauth(googleProfileDto.getSub(), 
                                                  googleProfileDto.getEmail(), 
                                                  SocialType.GOOGLE);
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

#### 5. GoogleService 구현
```java
// GoogleService.java
@Service
public class GoogleService {
    
    public AccessTokenDto getAccessToken(String code) {
        // Google OAuth2 토큰 엔드포인트로 POST 요청
        // client_id, client_secret, code, redirect_uri, grant_type 전송
        // 액세스 토큰 응답 받기
    }
    
    public GoogleProfileDto getGoogleProfile(String accessToken) {
        // Google User Info 엔드포인트로 GET 요청
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
현재 프로젝트에서는 **Spring Security OAuth2 방식**을 사용하고 있습니다.

#### 활성화된 코드
- `googleServerLogin()` 함수
- `GoogleOauth2LoginSuccess` 클래스
- `application.yml`의 OAuth2 설정

#### 비활성화된 코드 (정리 가능)
- `googleLogin()` 함수
- `GoogleRedirect.jsx` 컴포넌트
- `/member/google/login` 엔드포인트
- `GoogleService`, `GoogleProfileDto`, `AccessTokenDto`, `RedirectDto` 클래스들

---

## 정리 및 권장사항

### 1. 권장 방식
**Spring Security OAuth2 방식**을 권장합니다.
- 구현이 간단하고 안전함
- Spring Security의 검증된 보안 기능 활용
- 유지보수가 용이함

### 2. 정리할 코드
현재 프로젝트에서 다음 코드들을 정리할 수 있습니다:

#### 프론트엔드
```javascript
// MemberLogin.jsx에서 제거 가능
const googleUrl = "https://accounts.google.com/o/oauth2/auth"
const googleClientId = "your-google-client-id"
const googleRedirectUrl = "http://localhost:3000/oauth/google/redirect"
const googleScope = "openid email profile"

const googleLogin = () => {
  // 이 함수 제거 가능
}
```

#### 백엔드
```java
// MemberController.java에서 제거 가능
@PostMapping("/google/login")
public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto) {
  // 이 메서드 제거 가능
}

// 다음 클래스들도 제거 가능
// - GoogleService
// - GoogleProfileDto
// - AccessTokenDto
// - RedirectDto
```

### 3. 최종 구조
```
프론트엔드: googleServerLogin() → Spring OAuth2 엔드포인트
백엔드: GoogleOauth2LoginSuccess → JWT 토큰 발급
프론트엔드: 쿠키에서 토큰 추출 → 사용자 정보 조회
```

이렇게 정리하면 깔끔하고 안전한 Google OAuth 로그인 시스템을 구축할 수 있습니다. 