package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDataDAO {
    private static final Map<String, UserData> Users = new HashMap<>();

    public static void clearData() {
        Users.clear();
    }

    public static void createUser(String username, String email, String password) throws BadRequestException {
        if (Users.containsKey(username)) {
            throw new AlreadyTakenException("already taken");
        }
        if (username == null || username.isBlank() || email == null ||
                email.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("bad request");
        }
        Users.put(username, new UserData(username, email, password));
    }

    public static boolean isEmpty() {
        return Users.isEmpty();
    }

    public static boolean exists(String username) {
        return Users.containsKey(username);
    }

    public static void authorizedLogin(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("bad request");
        }
        if (!UserDataDAO.exists(username)) {
            throw new NotAuthorizedException("unauthorized");
        }
        if (!Users.get(username).password().equals(password)) {
            throw new NotAuthorizedException("unauthorized");
        }
    }
}
