package service;

import dataaccess.NotAuthorizedException;
import dataaccess.authDataDAO;

public class Logout {
    public static void logout(String token) {
        if (!authDataDAO.containsToken(token)) {
            throw new NotAuthorizedException("not authorized");
        }
        authDataDAO.removeAuthData(token);
    }
}
