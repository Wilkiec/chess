package dataaccess;

import model.authData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class authDataDAO {
    private static final Map<String, authData> auths = new HashMap<>();

    public static void clearData() {
        auths.clear();
    }

    public static authData generateAuthData(String username) {
        String newToken = UUID.randomUUID().toString();
        auths.put(newToken, new authData(newToken, username));
        return auths.get(newToken);
    }

    public static String usernameOfAuthToken(String authToken) {
        return auths.get(authToken).username();
    }

    public static void removeAuthData(String token) {
        auths.remove(token);
    }

    public static boolean containsToken(String token) {
        return auths.containsKey(token);
    }

    public static boolean containsUsername(String username) {
        for (authData data : auths.values()) {
            if (data.username().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty() {
        return auths.isEmpty();
    }
}
