package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameDataList;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.UserDataDAO.emptySQL;

public class GameDataDAO extends DatabaseManager {
    private static int gameIdentify = 1;

    public static void clearData() {
        String sqlScript = "TRUNCATE TABLE gameData";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sqlScript)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEmpty() {
        String sqlScript = "SELECT COUNT(*) FROM gameData";

        return emptySQL(sqlScript);
    }

    public static List<GameDataList> listGames() {
        String sqlScript = "SELECT * FROM gameData";
        List<GameDataList> games = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(sqlScript);
            try (var rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    int gameID = rs.getInt("gameID");
                    String usernameW = rs.getString("usernameWhite");
                    String usernameB = rs.getString("usernameBlack");
                    String gameName = rs.getString("gameName");

                    games.add(new GameDataList(gameID, usernameW, usernameB, gameName));
                }
                return games;
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static int createGame(String gameName) {
        String gameNameAlrExist = "SELECT 1 FROM gameData WHERE gameName = ?";
        String insertGameName = "INSERT INTO gameData (gameID, gameName, chessGame) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(gameNameAlrExist);
            preparedStatement.setString(1, gameName);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    throw new BadRequestException("bad request");
                }
            }

            int uniqueId = gameIdentify++;
            var ps = conn.prepareStatement(insertGameName);

            // turning chessGame into JSON.
            var serializer = new Gson();
            var chessGameJSON = serializer.toJson(new ChessGame());
            ps.setInt(1, uniqueId);
            ps.setString(2, gameName);
            ps.setString(3, chessGameJSON);
            try {
                ps.executeUpdate();
                return uniqueId;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void joinGame(int gameID, ChessGame.TeamColor teamColor, String username) {
        String userAlrExists;
        String insertGameName;
        insertGameName = "UPDATE gameData SET usernameWhite = ? WHERE gameID = ?";
        userAlrExists = "SELECT usernameWhite FROM gameData WHERE gameID = ? ";
        if (teamColor == ChessGame.TeamColor.BLACK) {
            insertGameName = "UPDATE gameData SET usernameBlack = ? WHERE gameID = ?";
            userAlrExists = "SELECT usernameBlack FROM gameData WHERE gameID = ? ";
        }

        try (var conn = DatabaseManager.getConnection()) {
            // checking to make sure the game has been created and not trying to join a non-existent game
            var preparedStatement = conn.prepareStatement(userAlrExists);
            preparedStatement.setInt(1, gameID);
            try (var rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    throw new BadRequestException("bad request");
                }

                // finding out if the color user wants to join is already taken
                String existingUsername = rs.getString(1);
                if (existingUsername != null) {
                    throw new AlreadyTakenException("already taken");
                }

                // inserting username into the specific team color
                preparedStatement = conn.prepareStatement(insertGameName);
                preparedStatement.setString(1, username);
                preparedStatement.setInt(2, gameID);

                try {
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
