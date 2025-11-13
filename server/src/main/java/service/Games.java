package service;

import chess.ChessGame;
import dataaccess.BadRequestException;
import dataaccess.NotAuthorizedException;
import dataaccess.AuthDataDAO;
import dataaccess.GameDataDAO;
import model.GameDataList;

import java.util.List;

public class Games {
    public static List<GameDataList> getGames(String token) {
        if (!AuthDataDAO.containsToken(token)) {
            throw new NotAuthorizedException("unauthorized");
        }
        return GameDataDAO.listGames();
    }

    public static int makeGame(String gameName, String token) {
        if (!AuthDataDAO.containsToken(token)) {
            throw new NotAuthorizedException("unauthorized");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("bad request");
        }
        return GameDataDAO.createGame(gameName);
    }

    public static void joinGame(int gameID, ChessGame.TeamColor teamColor, String token) {
        if (!AuthDataDAO.containsToken(token)) {
            throw new NotAuthorizedException("unauthorized");
        }
        if (teamColor == null) {
            throw new BadRequestException("bad request");
        }

        String username = AuthDataDAO.usernameOfAuthToken(token);
        GameDataDAO.joinGame(gameID, teamColor, username);
    }
}
