package dataaccess;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

import static dataaccess.AuthDataDAO.containsSQL;

public class UserDataDAO {
    public static void clearData() {
        String sqlScript = "TRUNCATE TABLE userData";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sqlScript)) {
                try {
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createUser(String username, String email, String password) throws BadRequestException {
        if (exists(username)) {
            throw new AlreadyTakenException("already taken");
        }
        if (username == null || username.isBlank() || email == null ||
                email.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("bad request");
        }

        String addNewUser = "INSERT INTO userData (username, email, password) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(addNewUser)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, hashedPassword);
                try {
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEmpty() {
        String sqlScript = "SELECT COUNT(*) FROM userData";

        return emptySQL(sqlScript);
    }

    static boolean emptySQL(String sqlScript) {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sqlScript)) {
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) == 0;
                    }
                    return false;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean exists(String username) {
        String sqlScript = "SELECT username FROM userData WHERE username = ?";

        return containsSQL(username, sqlScript);
    }

    public static void authorizedLogin(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("bad request");
        }

        String sqlScript = "SELECT password FROM userData WHERE username = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sqlScript)) {
                preparedStatement.setString(1, username);
                try (var rs = preparedStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new NotAuthorizedException("unauthorized");
                    }
                    if (!BCrypt.checkpw(password, rs.getString(1))) {
                        throw new NotAuthorizedException("unauthorized");
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
