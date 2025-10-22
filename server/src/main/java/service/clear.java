package service;

import dataaccess.authDataDAO;
import dataaccess.gameDataDAO;
import dataaccess.userDataDAO;

public final class clear {
    private clear() {
    }

    public static void clearApp() {
        userDataDAO.clearData();
        gameDataDAO.clearData();
        authDataDAO.clearData();
    }
}
