package dataaccess;

import chess.ChessGame;
import model.gameData;
import model.gameDataList;

import java.util.ArrayList;
import java.util.List;

public class gameDataDAO {
    private static final List<gameData> games = new ArrayList<>();
    private static final List<gameDataList> gamies = new ArrayList<>();
    private static int _gameID = 1000;

    public static void clearData() {
        gamies.clear();
        games.clear();
    }

    public static boolean isEmpty() {
        return games.isEmpty();
    }

    public static List<gameDataList> listGames() {
        return gamies;
    }

    public static int createGame(String gameName) {
        for (gameData game : games) {
            if (game.gameName().equals(gameName)) {
                throw new BadRequestException("bad request");
            }
        }
        gamies.add(new gameDataList(_gameID++, null, null, gameName));
        games.add(new gameData(_gameID++, null, null, gameName, new ChessGame()));
        return _gameID - 1;
    }

    public static void joinGame(int gameID, ChessGame.TeamColor teamColor, String username) {
        int check = 1;
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).gameID() == gameID) {
                gameData current = games.get(i);
                check = 0;
                if (teamColor == ChessGame.TeamColor.BLACK) {
                    if (current.blackUsername() != null) {
                        throw new AlreadyTakenException("already taken");
                    }
                    gamies.set(i, new gameDataList(current.gameID(), current.whiteUsername(), username, current.gameName()));
                    games.set(i, new gameData(current.gameID(), current.whiteUsername(), username, current.gameName(), current.game()));
                } else {
                    if (current.whiteUsername() != null) {
                        throw new AlreadyTakenException("already taken");
                    }
                    gamies.set(i, new gameDataList(current.gameID(), username, current.blackUsername(), current.gameName()));
                    games.set(i, new gameData(current.gameID(), username, current.blackUsername(), current.gameName(), current.game()));
                }
                break;
            }
        }
        if (check == 1) {
            throw new BadRequestException("bad request");
        }
    }
}
