package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;

public class InvalidCredentialsException extends BaseAppException {

    public InvalidCredentialsException() {
        super("Invalid email or password.",
                ErrorCode.INVALID_CREDENTIALS);
    }
}
