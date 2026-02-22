package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;

public class InvalidTokenException extends BaseAppException {

    public InvalidTokenException() {
        super("Invalid activation token.", ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(message, ErrorCode.INVALID_TOKEN);
    }
}
