package com.kh.shopit.controller;

import com.kh.shopit.exception.UserNotFoundException;
import com.kh.shopit.exception.UserAlreadyExistsException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

    // 사용자를 찾을 수 없는 경우 테스트
    @GetMapping("/user/{id}")
    public String getUser(@PathVariable Long id) {
        if (id <= 0) {
            throw new UserNotFoundException("ID가 " + id + "인 사용자를 찾을 수 없습니다.");
        }
        return "사용자 정보: " + id;
    }

    // 이미 존재하는 사용자 테스트
    @PostMapping("/user")
    public String createUser(@RequestParam String email) {
        if ("existing@test.com".equals(email)) {
            throw new UserAlreadyExistsException("이미 존재하는 이메일입니다: " + email);
        }
        return "사용자 생성 완료: " + email;
    }

    // 유효성 검사 테스트
    @PostMapping("/validate")
    public String validateUser(@Valid @RequestBody TestUserRequest request) {
        return "유효성 검사 통과: " + request.getName() + ", " + request.getAge();
    }

    // IllegalArgumentException 테스트
    @GetMapping("/illegal")
    public String testIllegalArgument(@RequestParam Integer number) {
        if (number < 0) {
            throw new IllegalArgumentException("음수는 허용되지 않습니다: " + number);
        }
        return "정상 처리: " + number;
    }

    // NullPointerException 테스트
    @GetMapping("/null")
    public String testNullPointer(@RequestParam(required = false) String value) {
        if (value == null) {
            throw new NullPointerException("값이 null입니다.");
        }
        return "정상 처리: " + value;
    }

    // 내부 서버 오류 테스트
    @GetMapping("/error")
    public String testInternalError() {
        throw new RuntimeException("의도적인 서버 오류");
    }

    // 유효성 검사를 위한 내부 클래스
    public static class TestUserRequest {
        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
        private int age;

        // Getter, Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
} 