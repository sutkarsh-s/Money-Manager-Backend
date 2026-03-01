package in.utkarshsingh.money.manager.security;

public final class UserContext {

    private static final ThreadLocal<UserInfo> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {}

    public static void setCurrentUser(String userId, String email) {
        CURRENT_USER.set(new UserInfo(userId, email));
    }

    public static String getCurrentEmail() {
        UserInfo info = CURRENT_USER.get();
        return info != null ? info.email() : null;
    }

    public static String getCurrentUserId() {
        UserInfo info = CURRENT_USER.get();
        return info != null ? info.userId() : null;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }

    public record UserInfo(String userId, String email) {}
}
