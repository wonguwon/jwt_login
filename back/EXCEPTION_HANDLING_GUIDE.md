# Shopit 예외 처리 시스템 가이드

## 개요

이 프로젝트는 Spring Boot 기반의 체계적인 예외 처리 시스템을 구현했습니다. 모든 예외는 일관된 형식으로 처리되며, 클라이언트에게 명확한 에러 메시지를 제공합니다.

## 구조

### 1. ErrorCode (예외 코드 관리)
- 모든 예외 상황을 enum으로 정의
- HTTP 상태 코드와 메시지를 함께 관리
- 확장 가능한 구조

### 2. BaseException (기본 예외 클래스)
- 모든 커스텀 예외의 부모 클래스
- ErrorCode를 포함하여 일관된 예외 처리

### 3. 구체적인 예외 클래스들
- `UserNotFoundException`: 사용자를 찾을 수 없을 때
- `UserAlreadyExistsException`: 이미 존재하는 사용자

### 4. ErrorResponse (에러 응답 DTO)
- 표준화된 에러 응답 형식
- 상태 코드, 메시지, 경로, 타임스탬프 포함

### 5. GlobalExceptionHandler (글로벌 예외 처리기)
- 모든 예외를 중앙에서 처리
- 로깅과 함께 적절한 응답 반환

## 사용법

### 1. 새로운 예외 추가하기

```java
// ErrorCode에 새로운 코드 추가
USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

// 새로운 예외 클래스 생성
public class OrderNotFoundException extends BaseException {
    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }
    
    public OrderNotFoundException(String message) {
        super(ErrorCode.ORDER_NOT_FOUND, message);
    }
}
```

### 2. 서비스에서 예외 발생시키기

```java
@Service
public class UserService {
    
    public User findUser(Long id) {
        User user = userRepository.findById(id)
            .orElse(null);
            
        if (user == null) {
            throw new UserNotFoundException("ID가 " + id + "인 사용자를 찾을 수 없습니다.");
        }
        
        return user;
    }
}
```

### 3. 컨트롤러에서 예외 처리

컨트롤러에서는 별도의 try-catch 없이 예외를 던지기만 하면 됩니다:

```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findUser(id);
        return ResponseEntity.ok(user);
    }
}
```

## 테스트 방법

애플리케이션을 실행한 후 다음 API들을 테스트해보세요:

### 1. 사용자 찾기 실패 테스트
```bash
GET /api/test/user/0
```
**예상 응답:**
```json
{
  "status": 404,
  "message": "ID가 0인 사용자를 찾을 수 없습니다.",
  "path": "/api/test/user/0",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 이미 존재하는 사용자 테스트
```bash
POST /api/test/user?email=existing@test.com
```
**예상 응답:**
```json
{
  "status": 409,
  "message": "이미 존재하는 이메일입니다: existing@test.com",
  "path": "/api/test/user",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 3. 유효성 검사 실패 테스트
```bash
POST /api/test/validate
Content-Type: application/json

{
  "name": "",
  "age": 0
}
```
**예상 응답:**
```json
{
  "status": 400,
  "message": "입력값 검증에 실패했습니다: {name=이름은 필수입니다., age=나이는 1 이상이어야 합니다.}",
  "path": "/api/test/validate",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 4. IllegalArgumentException 테스트
```bash
GET /api/test/illegal?number=-5
```
**예상 응답:**
```json
{
  "status": 400,
  "message": "음수는 허용되지 않습니다: -5",
  "path": "/api/test/illegal",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 5. NullPointerException 테스트
```bash
GET /api/test/null
```
**예상 응답:**
```json
{
  "status": 500,
  "message": "서버 내부 오류가 발생했습니다.",
  "path": "/api/test/null",
  "timestamp": "2024-01-01T12:00:00"
}
```

## 에러 응답 형식

모든 에러 응답은 다음 형식을 따릅니다:

```json
{
  "status": 400,
  "message": "에러 메시지",
  "path": "/api/endpoint",
  "timestamp": "2024-01-01T12:00:00"
}
```

- `status`: HTTP 상태 코드 (숫자)
- `message`: 사용자에게 보여줄 에러 메시지
- `path`: 에러가 발생한 요청 경로
- `timestamp`: 에러 발생 시각

## 로깅

모든 예외는 `GlobalExceptionHandler`에서 자동으로 로깅됩니다:
- `BaseException`: 에러 레벨로 로깅
- `MethodArgumentNotValidException`: 에러 레벨로 로깅
- `Exception`: 에러 레벨로 로깅 (스택 트레이스 포함)

## 장점

1. **일관성**: 모든 예외가 동일한 형식으로 처리됨
2. **유지보수성**: 예외 처리 로직이 한 곳에 집중됨
3. **확장성**: 새로운 예외 타입을 쉽게 추가 가능
4. **보안**: 내부 오류 정보가 클라이언트에 노출되지 않음
5. **디버깅**: 상세한 로깅으로 문제 추적 용이 