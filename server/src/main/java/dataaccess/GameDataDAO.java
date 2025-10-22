package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.GameDataList;

import java.util.ArrayList;
import java.util.List;

public class GameDataDAO {
    private static final List<GameData> Games = new ArrayList<>();
    private static final List<GameDataList> Gamies = new ArrayList<>();
    private static int gameIdentify = 1000;

    public static void clearData() {
        Gamies.clear();
        Games.clear();
    }

    public static boolean isEmpty() {
        return Games.isEmpty();
    }

    public static List<GameDataList> listGames() {
        return Gamies;
    }

    public static int createGame(String gameName) {
        for (GameData game : Games) {
            if (game.gameName().equals(gameName)) {
                throw new BadRequestException("bad request");
            }
        }
        Gamies.add(new GameDataList(gameIdentify++, null, null, gameName));
        Games.add(new GameData(gameIdentify++, null, null, gameName, new ChessGame()));
        return gameIdentify - 1;
    }

    public static void joinGame(int gameID, ChessGame.TeamColor teamColor, String username) {
        int check = 1;
        for (int i = 0; i < Games.size(); i++) {
            if (Games.get(i).gameID() == gameID) {
                GameData current = Games.get(i);
                check = 0;
                if (teamColor == ChessGame.TeamColor.BLACK) {
                    if (current.blackUsername() != null) {
                        throw new AlreadyTakenException("already taken");
                    }
                    Gamies.set(i, new GameDataList(current.gameID(), current.whiteUsername(), username, current.gameName()));
                    Games.set(i, new GameData(current.gameID(), current.whiteUsername(), username, current.gameName(), current.game()));
                } else {
                    if (current.whiteUsername() != null) {
                        throw new AlreadyTakenException("already taken");
                    }
                    Gamies.set(i, new GameDataList(current.gameID(), username, current.blackUsername(), current.gameName()));
                    Games.set(i, new GameData(current.gameID(), username, current.blackUsername(), current.gameName(), current.game()));
                }
                break;
            }
        }
        if (check == 1) {
            throw new BadRequestException("bad request");
        }
    }
}
