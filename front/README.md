# OAuth React Frontend

Vue.js로 작성된 OAuth 로그인 프로젝트를 React로 마이그레이션한 프로젝트입니다.

## 기능

- 일반 회원가입/로그인
- Google OAuth 로그인
- Kakao OAuth 로그인
- JWT 토큰 기반 인증

## 설치 및 실행

### 의존성 설치
```bash
npm install
```

### 개발 서버 실행
```bash
npm run dev
```

개발 서버는 http://localhost:3000 에서 실행됩니다.

## 프로젝트 구조

```
src/
├── components/
│   ├── HeaderComponent.jsx    # 헤더 컴포넌트 (로그인/로그아웃 버튼)
│   ├── MemberCreate.jsx       # 회원가입 페이지
│   ├── MemberLogin.jsx        # 로그인 페이지
│   ├── GoogleRedirect.jsx     # Google OAuth 리다이렉트 처리
│   └── KakaoRedirect.jsx      # Kakao OAuth 리다이렉트 처리
├── assets/
│   ├── google_login.png       # Google 로그인 버튼 이미지
│   └── kakao_login.png        # Kakao 로그인 버튼 이미지
├── App.jsx                    # 메인 앱 컴포넌트
└── main.jsx                   # 앱 진입점
```

## 사용된 기술

- React 18
- React Router DOM
- Material-UI
- Styled Components
- Axios
- js-cookie

## API 엔드포인트

백엔드 서버는 http://localhost:8080 에서 실행되어야 합니다.

- `POST /member/create` - 회원가입
- `POST /member/doLogin` - 일반 로그인
- `POST /member/google/doLogin` - Google OAuth 로그인
- `POST /member/kakao/doLogin` - Kakao OAuth 로그인

## 라우팅

- `/` - 홈페이지
- `/member/create` - 회원가입
- `/member/login` - 로그인
- `/oauth/google/redirect` - Google OAuth 리다이렉트
- `/oauth/kakao/redirect` - Kakao OAuth 리다이렉트
