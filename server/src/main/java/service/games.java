package service;

import chess.ChessGame;
import dataaccess.BadRequestException;
import dataaccess.NotAuthorizedException;
import dataaccess.authDataDAO;
import dataaccess.gameDataDAO;
import model.gameDataList;

import java.util.List;

public class games {
    public static List<gameDataList> getGames(String token) {
        if (!authDataDAO.containsToken(token)) {
            throw new NotAuthorizedException("unauthorized");
        }
        return gameDataDAO.listGames();
    }

    public static int makeGame(String gameName, String token) {
        if (!authDataDAO.containsToken(token)) {
            throw new NotAuthorizedException("unauthorized");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("bad request");
        }
        return gameDataDAO.createGame(gameName);
    }

    public static void joinGame(int gameID, ChessGame.TeamColor teamColor, String token) {
        if (!authDataDAO.containsToken(token)) {
            throw new NotAuthorizedException("unauthorized");
        }
        if (teamColor == null) {
            throw new BadRequestException("bad request");
        }

        String username = authDataDAO.usernameOfAuthToken(token);
        gameDataDAO.joinGame(gameID, teamColor, username);
    }
}
