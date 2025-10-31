package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.GameDataList;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameDataDAO extends DatabaseManager {
    private static final List<GameData> GAMES = new ArrayList<>();
    private static final List<GameDataList> GAMIES = new ArrayList<>();
    private static int gameIdentify = 1000;

    public static void clearData() throws Exception, DataAccessException {
        String sqlScript = "TRUNCATE TABLE gameData";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sqlScript)) {
                preparedStatement.executeUpdate();
            }
        }
    }

    public static boolean isEmpty() {
        String sqlScript = "SELECT COUNT(*) FROM gameData";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(sqlScript);
            var rs = preparedStatement.executeQuery();
            return rs.getInt(1) == 1;
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<GameDataList> listGames() {
        String sqlScript = "SELECT * FROM gameData";
        List<GameDataList> games = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(sqlScript);
            var rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int gameID = rs.getInt("gameID");
                String usernameW = rs.getString("usernameWhite");
                String usernameB = rs.getString("usernameBlack");
                String gameName = rs.getString("gameName");

                games.add(new GameDataList(gameID, usernameW, usernameB, gameName));
            }
            return games;
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static int createGame(String gameName) {
        String sqlScript = "SELECT 1 FROM gameData WHERE gameName = " + gameName;
        String insertGameName = "INSERT INTO gameData (gameID, gameName) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(sqlScript);
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                throw new BadRequestException("bad request");
            }
            int uniqueId = gameIdentify++;
            var ps = conn.prepareStatement(insertGameName);
            ps.setString(1, gameName);
            ps.setInt(2, uniqueId);
            ps.executeUpdate();
            return uniqueId;
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
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
