package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;

public class EmailAlreadyExistsException extends BaseAppException {

    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email,
                ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
