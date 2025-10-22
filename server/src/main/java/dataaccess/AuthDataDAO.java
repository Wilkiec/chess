package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDataDAO {
    private static final Map<String, AuthData> AUTHS = new HashMap<>();

    public static void clearData() {
        AUTHS.clear();
    }

    public static AuthData generateAuthData(String username) {
        String newToken = UUID.randomUUID().toString();
        AUTHS.put(newToken, new AuthData(newToken, username));
        return AUTHS.get(newToken);
    }

    public static String usernameOfAuthToken(String authToken) {
        return AUTHS.get(authToken).username();
    }

    public static void removeAuthData(String token) {
        AUTHS.remove(token);
    }

    public static boolean containsToken(String token) {
        return AUTHS.containsKey(token);
    }

    public static boolean containsUsername(String username) {
        for (AuthData data : AUTHS.values()) {
            if (data.username().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty() {
        return AUTHS.isEmpty();
    }
}
