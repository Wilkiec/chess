package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.GameDataList;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.UserDataDAO.emptySQL;

public class GameDataDAO extends DatabaseManager {

    public static void clearData() {
        String sqlScript = "TRUNCATE TABLE gameData";
        String resetSequenceSql = "ALTER TABLE gameData AUTO_INCREMENT = 1";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sqlScript)) {
                preparedStatement.executeUpdate();
            }
            try (var preparedStatement = conn.prepareStatement(resetSequenceSql)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resign(int gameId, GameData gameData, boolean whiteWon) {
        GameData resigned = new GameData(gameData.gameID(),gameData.whiteUsername(),
                gameData.blackUsername(), gameData.gameName(), gameData.game(), true, whiteWon);

        updateGame(gameId, resigned);
    }

    public static void updateGame(int gameId, GameData game) {
        String updateGameSql = """
        UPDATE gameData SET usernameWhite = ?, usernameBlack = ?, gameName = ?, chessGame = ?, gameOver = ?, whiteWon = ? WHERE gameID = ?
        """;

        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(updateGameSql)) {
                var serializer = new Gson();
                var chessGameJSON = serializer.toJson(game.game());
                ps.setString(1, game.whiteUsername());
                ps.setString(2, game.blackUsername());
                ps.setString(3, game.gameName());
                ps.setString(4, chessGameJSON);
                ps.setBoolean(5, game.gameOver());
                ps.setBoolean(6, game.whiteWon());
                ps.setInt(7, gameId);

                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new BadRequestException("Unable to find game with given gameID");
                }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removePlayer(boolean white, int gameId) {
        String removePlayer = "UPDATE gameData SET usernameWhite = NULL WHERE gameID = ?";
        if (!white) {
            removePlayer = "UPDATE gameData SET usernameBlack = NULL WHERE gameID = ?";
        }

        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(removePlayer)) {
                preparedStatement.setInt(1, gameId);
                int updatedRows = preparedStatement.executeUpdate();
                if (updatedRows == 0) {
                    throw new BadRequestException("unable to find game with given gameID");
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

        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(sqlScript)) {
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

    public static GameData getGame(int gameId) {
        String findGame = "SELECT usernameWhite, usernameBlack, gameName, chessGame, gameOver, whiteWon FROM gameData WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(findGame)) {
            preparedStatement.setInt(1, gameId);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    String gameName = rs.getString("gameName");
                    String jsonGame = rs.getString("chessGame");
                    String whiteUser = rs.getString("usernameWhite");
                    String blackUser = rs.getString("usernameBlack");
                    boolean gameOver = rs.getBoolean("gameOver");
                    boolean whiteWon = rs.getBoolean("whiteWon");

                    ChessGame game = new Gson().fromJson(jsonGame, ChessGame.class);

                    return new GameData(gameId, whiteUser, blackUser, gameName, game, gameOver, whiteWon);
                    }
                } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException | DataAccessException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    public static int createGame(String gameName) {
        String gameNameAlrExist = "SELECT 1 FROM gameData WHERE gameName = ?";
        String insertGameName = "INSERT INTO gameData (gameName, chessGame, gameOver, whiteWon) VALUES (?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(gameNameAlrExist)) {
            preparedStatement.setString(1, gameName);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    throw new BadRequestException("Game Name already exists");
                }
            }

            var ps = conn.prepareStatement(insertGameName, Statement.RETURN_GENERATED_KEYS);

            // turning chessGame into JSON.
            var serializer = new Gson();
            var chessGameJSON = serializer.toJson(new ChessGame());
            ps.setString(1, gameName);
            ps.setString(2, chessGameJSON);
            ps.setBoolean(3, false);
            ps.setBoolean(4, false);

            ps.executeUpdate();
            try (var generatedKeys = ps.getGeneratedKeys()){
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("No ID obtained");
                }
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

        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(userAlrExists)) {
            // checking to make sure the game has been created and not trying to join a non-existent game
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
                try (var preparedStatement1 = conn.prepareStatement(insertGameName)) {
                    preparedStatement1.setString(1, username);
                    preparedStatement1.setInt(2, gameID);

                    try {
                        preparedStatement1.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
