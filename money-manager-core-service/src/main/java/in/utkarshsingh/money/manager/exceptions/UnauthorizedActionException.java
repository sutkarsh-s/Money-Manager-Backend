package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;

public class UnauthorizedActionException extends BaseAppException {

    public UnauthorizedActionException(String message) {
        super(message, ErrorCode.UNAUTHORIZED_ACTION);
    }
}
