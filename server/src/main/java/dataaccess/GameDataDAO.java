package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.GameDataList;

import java.util.ArrayList;
import java.util.List;

public class GameDataDAO {
    private static final List<GameData> GAMES = new ArrayList<>();
    private static final List<GameDataList> GAMIES = new ArrayList<>();
    private static int gameIdentify = 1000;

    public static void clearData() {
        GAMIES.clear();
        GAMES.clear();
    }

    public static boolean isEmpty() {
        return GAMES.isEmpty();
    }

    public static List<GameDataList> listGames() {
        return GAMIES;
    }

    public static int createGame(String gameName) {
        for (GameData game : GAMES) {
            if (game.gameName().equals(gameName)) {
                throw new BadRequestException("bad request");
            }
        }
        GAMIES.add(new GameDataList(gameIdentify++, null, null, gameName));
        GAMES.add(new GameData(gameIdentify++, null, null, gameName, new ChessGame()));
        return gameIdentify - 1;
    }

    public static void joinGame(int gameID, ChessGame.TeamColor teamColor, String username) {
        int check = 1;
        for (int i = 0; i < GAMES.size(); i++) {
            if (GAMES.get(i).gameID() == gameID) {
                GameData current = GAMES.get(i);
                check = 0;
                if (teamColor == ChessGame.TeamColor.BLACK) {
                    if (current.blackUsername() != null) {
                        throw new AlreadyTakenException("already taken");
                    }
                    GAMIES.set(i, new GameDataList(current.gameID(), current.whiteUsername(), username, current.gameName()));
                    GAMES.set(i, new GameData(current.gameID(), current.whiteUsername(), username, current.gameName(), current.game()));
                } else {
                    if (current.whiteUsername() != null) {
                        throw new AlreadyTakenException("already taken");
                    }
                    GAMIES.set(i, new GameDataList(current.gameID(), username, current.blackUsername(), current.gameName()));
                    GAMES.set(i, new GameData(current.gameID(), username, current.blackUsername(), current.gameName(), current.game()));
                }
                break;
            }
        }
        if (check == 1) {
            throw new BadRequestException("bad request");
        }
    }
}
