package dataaccess;

import model.userData;

import java.util.HashMap;
import java.util.Map;

public class userDataDAO {
    private static final Map<String, userData> users = new HashMap<>();

    public static void clearData() {
        users.clear();
    }

    public static void createUser(String username, String email, String password) throws BadRequestException {
        if (users.containsKey(username)) {
            throw new AlreadyTakenException("already taken");
        }
        if (username == null || username.isBlank() || email == null ||
                email.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("bad request");
        }
        users.put(username, new userData(username, email, password));
    }

    public static boolean isEmpty() {
        return users.isEmpty();
    }

    public static boolean exists(String username) {
        return users.containsKey(username);
    }

    public static void authorizedLogin(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("bad request");
        }
        if (!userDataDAO.exists(username)) {
            throw new NotAuthorizedException("unauthorized");
        }
        if (!users.get(username).password().equals(password)) {
            throw new NotAuthorizedException("unauthorized");
        }
    }
}
