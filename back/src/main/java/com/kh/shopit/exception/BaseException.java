package com.kh.shopit.exception;

import lombok.Getter;

//abstract: 이 클래스는 직접 사용할 수 없고, 구체 예외 클래스가 상속해서 사용해야 함.
@Getter
public abstract class BaseException extends RuntimeException {
    //RuntimeException을 상속: 체크 예외가 아닌 언체크 예외로 처리하겠다는 의도.

    private final ErrorCode errorCode;
//    예외 발생 시 구체적인 에러 정보를 담은 ErrorCode Enum 객체를 저장.
//    이걸 통해 HTTP 상태 코드, 에러 메시지, 로그 구분 코드 등 통합 관리 가능.


    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;

    }

//    ErrorCode는 유지하되, 메시지는 커스텀 문자열을 사용할 수 있음.
//    특정 상황에 더 구체적인 설명을 하고 싶을 때 사용.
    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    //예외의 원인(cause)을 함께 전달.
    //우리가 만든 예외(BaseException)로 감싸서 다시 던지는 것.
    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
} 