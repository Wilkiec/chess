package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword)) {
            try (var preparedStatement = conn.prepareStatement(statement) ) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }

        String createGameDataSql = """
                CREATE TABLE IF NOT EXISTS gameData (
                    gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    usernameWhite VARCHAR(255),
                    usernameBlack VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    chessGame TEXT,
                    gameOver BOOLEAN,
                    whiteWon BOOLEAN
                );
                """;

        String createUserDataSql = """
                CREATE TABLE IF NOT EXISTS userData (
                    username VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL
                );
                """;

        String createAuthDataSql = """
                CREATE TABLE IF NOT EXISTS authData (
                    authToken VARCHAR(255) NOT NULL,
                    username VARCHAR(255) NOT NULL
                );
                """;

        var dbConnectionUrl = connectionUrl + "/" + databaseName;
        try (var conn = DriverManager.getConnection(dbConnectionUrl, dbUsername, dbPassword)) {
            // creating gameDataTable
            try (var preparedStatement = conn.prepareStatement(createGameDataSql)) {
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to create table 'gameData': " + ex.getMessage(), ex);
            }

            // creating userDataTable
            try (var preparedStatement = conn.prepareStatement(createUserDataSql)) {
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to create table 'userData': " + ex.getMessage(), ex);
            }

            // creating authDataTable
            try (var preparedStatement = conn.prepareStatement(createAuthDataSql)) {
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to create table 'userData': " + ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create connection to db" + ex.getMessage(), ex);
        }

        try (var conn = DriverManager.getConnection(dbConnectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(createUserDataSql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create table 'gameData': " + ex.getMessage(), ex);
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     *
     */
    static Connection getConnection() throws DataAccessException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}
