package in.utkarshsingh.money.manager.exceptions;
import in.utkarshsingh.money.manager.enums.ErrorCode;

public class ProfileRegistrationException extends BaseAppException {

        private static final String DEFAULT_MESSAGE =
                "Profile registration failed. Please try again later.";

        public ProfileRegistrationException() {
            super(DEFAULT_MESSAGE, ErrorCode.PROFILE_REGISTRATION_FAILED);
        }

        public ProfileRegistrationException(String customMessage) {
            super(customMessage, ErrorCode.PROFILE_REGISTRATION_FAILED);
        }
}
