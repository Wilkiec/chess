package service;

import dataaccess.BadRequestException;
import dataaccess.authDataDAO;
import dataaccess.userDataDAO;
import model.authData;


public final class registration {
    private registration() {
    }

    public static authData register(String username, String password, String email) throws BadRequestException {
        userDataDAO.createUser(username, email, password);
        return authDataDAO.generateAuthData(username);
    }
}
