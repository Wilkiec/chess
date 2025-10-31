package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static dataaccess.UserDataDAO.emptySQL;

public class AuthDataDAO {
    public static void clearData() {
        String sqlScript = "TRUNCATE TABLE authData";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sqlScript)) {
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

    public static AuthData generateAuthData(String username) {
        String newToken = UUID.randomUUID().toString();

        String insertNewAuthToken = "INSERT INTO authData (authToken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            var ps = conn.prepareStatement(insertNewAuthToken);
            ps.setString(1, newToken);
            ps.setString(2, username);
            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        return new AuthData(newToken, username);
    }

    public static String usernameOfAuthToken(String authToken) {
        String getUsername = "SELECT username From authData WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(getUsername)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    } else {
                        throw new BadRequestException("No such AuthToken");
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeAuthData(String token) {
        String deleteUserAuth = "DELETE FROM authData WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(deleteUserAuth)) {
                ps.setString(1, token);
                try {
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean containsToken(String token) {
        String deleteUserAuth = "SELECT * FROM authData WHERE authToken = ?";

        return containsSQL(token, deleteUserAuth);
    }

    static boolean containsSQL(String token, String deleteUserAuth) {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(deleteUserAuth)) {
                ps.setString(1, token);
                try (var rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean containsUsername(String username) {
        String deleteUserAuth = "SELECT * FROM authData WHERE username = ?";

        return containsSQL(username, deleteUserAuth);
    }

    public static boolean isEmpty() {
        String sqlScript = "SELECT COUNT(*) FROM authData";

        return emptySQL(sqlScript);
    }
}
