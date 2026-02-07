package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;
public class UserNotFoundException extends BaseAppException {

    public UserNotFoundException(String email) {
        super("No account found with email: " + email,
                ErrorCode.USER_NOT_FOUND);
    }
}

