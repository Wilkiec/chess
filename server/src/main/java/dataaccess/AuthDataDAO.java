package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDataDAO {
    private static final Map<String, AuthData> Auths = new HashMap<>();

    public static void clearData() {
        Auths.clear();
    }

    public static AuthData generateAuthData(String username) {
        String newToken = UUID.randomUUID().toString();
        Auths.put(newToken, new AuthData(newToken, username));
        return Auths.get(newToken);
    }

    public static String usernameOfAuthToken(String authToken) {
        return Auths.get(authToken).username();
    }

    public static void removeAuthData(String token) {
        Auths.remove(token);
    }

    public static boolean containsToken(String token) {
        return Auths.containsKey(token);
    }

    public static boolean containsUsername(String username) {
        for (AuthData data : Auths.values()) {
            if (data.username().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty() {
        return Auths.isEmpty();
    }
}
