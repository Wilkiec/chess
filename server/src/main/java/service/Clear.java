package service;

import dataaccess.AuthDataDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDataDAO;
import dataaccess.UserDataDAO;

public final class Clear {
    private Clear() {
    }

    public static void clearApp() throws Exception, DataAccessException {
        UserDataDAO.clearData();
        GameDataDAO.clearData();
        AuthDataDAO.clearData();
    }
}
