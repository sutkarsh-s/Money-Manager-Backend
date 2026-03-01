package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;

public class AccountLockedException extends BaseAppException {

    private static final String DEFAULT_MESSAGE =
            "Your account is locked due to multiple failed login attempts. Please try again later.";

    public AccountLockedException() {
        super(DEFAULT_MESSAGE, ErrorCode.ACCOUNT_LOCKED);
    }
}
