package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class AccountNotActiveException extends BaseAppException {

    public AccountNotActiveException(String email) {
        super("Account is not activated for email: " + email,
                ErrorCode.ACCOUNT_NOT_ACTIVE);
    }
}
