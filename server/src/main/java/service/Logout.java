package service;

import dataaccess.NotAuthorizedException;
import dataaccess.AuthDataDAO;

public class Logout {
    public static void logout(String token) {
        if (!AuthDataDAO.containsToken(token)) {
            throw new NotAuthorizedException("not authorized");
        }
        AuthDataDAO.removeAuthData(token);
    }
}
