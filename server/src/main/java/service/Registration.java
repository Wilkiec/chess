package service;

import dataaccess.BadRequestException;
import dataaccess.AuthDataDAO;
import dataaccess.UserDataDAO;
import model.AuthData;


public final class Registration {
    private Registration() {
    }

    public static AuthData register(String username, String password, String email) throws BadRequestException {
        UserDataDAO.createUser(username, email, password);
        return AuthDataDAO.generateAuthData(username);
    }
}
