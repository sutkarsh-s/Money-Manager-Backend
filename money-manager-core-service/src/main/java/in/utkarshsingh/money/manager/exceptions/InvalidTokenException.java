package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseAppException {
    public InvalidTokenException() {
        super("Invalid activation token.",
                ErrorCode.INVALID_TOKEN);
    }
}
