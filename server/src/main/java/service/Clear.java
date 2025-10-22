package service;

import dataaccess.AuthDataDAO;
import dataaccess.GameDataDAO;
import dataaccess.UserDataDAO;

public final class Clear {
    private Clear() {
    }

    public static void clearApp() {
        UserDataDAO.clearData();
        GameDataDAO.clearData();
        AuthDataDAO.clearData();
    }
}
