package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDataDAO {
    private static final Map<String, UserData> USERS = new HashMap<>();

    public static void clearData() {
        USERS.clear();
    }

    public static void createUser(String username, String email, String password) throws BadRequestException {
        if (USERS.containsKey(username)) {
            throw new AlreadyTakenException("already taken");
        }
        if (username == null || username.isBlank() || email == null ||
                email.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("bad request");
        }
        USERS.put(username, new UserData(username, email, password));
    }

    public static boolean isEmpty() {
        return USERS.isEmpty();
    }

    public static boolean exists(String username) {
        return USERS.containsKey(username);
    }

    public static void authorizedLogin(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("bad request");
        }
        if (!UserDataDAO.exists(username)) {
            throw new NotAuthorizedException("unauthorized");
        }
        if (!USERS.get(username).password().equals(password)) {
            throw new NotAuthorizedException("unauthorized");
        }
    }
}
