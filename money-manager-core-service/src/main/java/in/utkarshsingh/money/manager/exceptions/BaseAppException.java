package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;
import lombok.Getter;

@Getter
public abstract class BaseAppException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseAppException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
