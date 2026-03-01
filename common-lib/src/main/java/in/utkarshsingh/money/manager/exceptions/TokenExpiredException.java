package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;

public class TokenExpiredException extends BaseAppException {

    public TokenExpiredException() {
        super("Activation token has expired.", ErrorCode.TOKEN_EXPIRED);
    }

    public TokenExpiredException(String message) {
        super(message, ErrorCode.TOKEN_EXPIRED);
    }
}
