package service;

import dataaccess.AuthDataDAO;
import dataaccess.UserDataDAO;
import model.AuthData;

public class Login {
    public static AuthData login(String username, String password) {
        UserDataDAO.authorizedLogin(username, password);
        return AuthDataDAO.generateAuthData(username);
    }
}
