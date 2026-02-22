package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;

public class InvalidPasswordException extends BaseAppException {

    public InvalidPasswordException() {
        super("Current password is incorrect.", ErrorCode.INVALID_PASSWORD);
    }
}
