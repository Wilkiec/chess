package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;


public class ServerFacadeTests {

    private static String ServerUrl;
    private static Server server;
    private static ServerFacade Facade;
    
    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        ServerUrl = "http://localhost:" + port;
        Facade = new ServerFacade(ServerUrl);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() {
        // This method now calls the /db endpoint to clear the database
        try {
            URI uri = new URI(ServerUrl + "/db");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("DELETE");
            http.connect();

            int statusCode = http.getResponseCode();
            if (statusCode != 200) {
                String responseBody;
                try (InputStream errorMessage = http.getErrorStream()) {
                    responseBody = new String(errorMessage.readAllBytes(), UTF_8);
                } catch (Exception e) {
                    responseBody = "Unable to read error stream: " + e.getMessage();
                }
                throw new RuntimeException("Failed to clear database. Status: " + statusCode + ", Body: " + responseBody);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear database: " + e.getMessage(), e);
        }
    }

    private String generateUniqueUsername() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateUniqueEmail() {
        return "email-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    @Test
    public void registerPositive() {
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();

        Assertions.assertDoesNotThrow(() -> {
            AuthData auth = Facade.register(username, "password123", email);
            Assertions.assertNotNull(auth);
            Assertions.assertEquals(username, auth.username());
            Assertions.assertNotNull(auth.authToken());
        });
    }

    @Test
    public void registerNegative() {
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();

        Assertions.assertDoesNotThrow(() -> {
            Facade.register(username, "password123", email);
        });

        Assertions.assertThrows(ResponseException.class, () -> Facade.register(username, "newpass", "new-email@test.com"));
    }

    @Test
    @DisplayName("Login Success")
    public void loginPositive() throws ResponseException {
        String username = generateUniqueUsername();
        String password = "myPassword";
        Facade.register(username, password, generateUniqueEmail());

        AuthData auth = Facade.login(username, password);
        Assertions.assertNotNull(auth);
        Assertions.assertEquals(username, auth.username());
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void loginNegativeWrongPassword() throws ResponseException {
        String username = generateUniqueUsername();
        String password = "myPassword";
        Facade.register(username, password, generateUniqueEmail());

        Assertions.assertThrows(ResponseException.class, () -> Facade.login(username, "wrongPassword"));
    }

    @Test
    public void loginNegativeUserNotFound() {
        Assertions.assertThrows(ResponseException.class, () -> Facade.login("nonExistentUser", "anyPassword"));
    }

    @Test
    public void logoutPositive() throws ResponseException {
        String username = generateUniqueUsername();
        AuthData auth = Facade.register(username, "pass", generateUniqueEmail());

        Assertions.assertDoesNotThrow(() -> Facade.logout(auth.authToken()));
    }

    @Test
    public void logoutNegative() {
        Assertions.assertThrows(ResponseException.class, () -> Facade.logout("this-is-a-fake-token"));
    }

    @Test
    public void createGamePositive() throws ResponseException {
        AuthData auth = Facade.register(generateUniqueUsername(), "pass", generateUniqueEmail());

        Assertions.assertDoesNotThrow(() -> Facade.createGame(auth.authToken(), "My New Game"));
    }

    @Test
    public void createGameNegative() {
        Assertions.assertThrows(ResponseException.class, () -> Facade.createGame("fake-token", "My Game"));
    }

    @Test
    public void listGamePositive() throws ResponseException {
        AuthData auth = Facade.register(generateUniqueUsername(), "pass", generateUniqueEmail());
        Facade.createGame(auth.authToken(), "Game1");
        Facade.createGame(auth.authToken(), "Game2");

        GameListResponse response = Facade.listGame(auth.authToken());
        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.games().size());
    }

    @Test
    public void listGameNegative() {
        Assertions.assertThrows(ResponseException.class, () -> Facade.listGame("fake-token"));
    }

    private int createAndFindGameId(String authToken, String gameName) throws ResponseException {
        Facade.createGame(authToken, gameName);
        GameListResponse list = Facade.listGame(authToken);
        for (GameData game : list.games()) {
            if (game.gameName().equals(gameName)) {
                return game.gameID();
            }
        }
        throw new RuntimeException("Test setup failed: Could not find created game.");
    }

    @Test
    public void joinGamePositive() throws ResponseException {
        AuthData auth = Facade.register(generateUniqueUsername(), "pass", generateUniqueEmail());
        String gameName = "my-joinable-game";

        final int finalGameId = createAndFindGameId(auth.authToken(), gameName);
        Assertions.assertDoesNotThrow(() -> Facade.joinGame(auth.authToken(), finalGameId, "white"));
    }

    @Test
    public void joinGameNegativeSpotTaken() throws ResponseException {
        AuthData auth1 = Facade.register(generateUniqueUsername(), "pass1", generateUniqueEmail());
        String gameName = "game-for-spot-taken-test";
        int gameId = createAndFindGameId(auth1.authToken(), gameName);
        Facade.joinGame(auth1.authToken(), gameId, "white");

        AuthData auth2 = Facade.register(generateUniqueUsername(), "pass2", generateUniqueEmail());

        final int finalGameId = gameId;
        Assertions.assertThrows(ResponseException.class, () -> Facade.joinGame(auth2.authToken(), finalGameId, "white"));
    }

    @Test
    public void joinGameNegativeBadId() throws ResponseException {
        AuthData auth = Facade.register(generateUniqueUsername(), "pass", generateUniqueEmail());

        Assertions.assertThrows(ResponseException.class, () -> Facade.joinGame(auth.authToken(), 99999, "black"));
    }
}
