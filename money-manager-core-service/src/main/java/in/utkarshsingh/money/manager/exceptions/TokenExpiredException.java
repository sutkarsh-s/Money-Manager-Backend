package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends BaseAppException {
    public TokenExpiredException() {
        super("Activation token has expired.",
                ErrorCode.TOKEN_EXPIRED);
    }
}
