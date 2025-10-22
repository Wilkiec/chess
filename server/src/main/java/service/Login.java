package service;

import dataaccess.authDataDAO;
import dataaccess.userDataDAO;
import model.authData;

public class Login {
    public static authData login(String username, String password) {
        userDataDAO.authorizedLogin(username, password);
        return authDataDAO.generateAuthData(username);
    }
}
