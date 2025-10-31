package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameDataList;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameDataDAO extends DatabaseManager {
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
        String gameNameAlrExist = "SELECT 1 FROM gameData WHERE gameName = " + gameName;
        String insertGameName = "INSERT INTO gameData (gameID, gameName, chessGame) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(gameNameAlrExist);
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                throw new BadRequestException("bad request");
            }
            int uniqueId = gameIdentify++;
            var ps = conn.prepareStatement(insertGameName);

            // turning chessGame into JSON.
            var serializer = new Gson();
            var chessGameJSON = serializer.toJson(new ChessGame());
            ps.setString(1, gameName);
            ps.setInt(2, uniqueId);
            ps.setString(3, chessGameJSON);
            ps.executeUpdate();
            return uniqueId;
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void joinGame(int gameID, ChessGame.TeamColor teamColor, String username) {
        String gameIdExist = "SELECT 1 FROM gameData WHERE gameID = " + gameID;

        String insertGameName;
        insertGameName = "UPDATE gameData SET usernameWhite = ? WHERE gameID = ?";
        if (teamColor == ChessGame.TeamColor.BLACK) {
            insertGameName = "UPDATE gameData SET usernameBlack = ? WHERE gameID = ?";
        }

        try (var conn = DatabaseManager.getConnection()) {
            // checking to make sure the game has been created and not trying to join a non existant game
            var preparedStatement = conn.prepareStatement(gameIdExist);
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                throw new BadRequestException("bad request");
            }

            // inserting username into the specific team color
            preparedStatement = conn.prepareStatement(insertGameName);
            preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
