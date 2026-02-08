package in.utkarshsingh.money.manager.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_ACTIVE(HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(HttpStatus.LOCKED),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT),
    PROFILE_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(HttpStatus.GONE),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
