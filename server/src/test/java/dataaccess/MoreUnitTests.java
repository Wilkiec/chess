package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameDataList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoreUnitTests {
    @BeforeAll
    public static void makeDataBase() throws DataAccessException {
        DatabaseManager.createDatabase();
    }

    @BeforeEach
    public void clearData() throws Exception, DataAccessException {
        Clear.clearApp();
    }

    @Test
    public void clearPositiveTest() throws Exception, DataAccessException {
        Registration.register("jamil", "corben.wilkie55@gmail.com", "12345");
        Clear.clearApp();

        assert UserDataDAO.isEmpty();
        assert AuthDataDAO.isEmpty();
        assert GameDataDAO.isEmpty();
    }

    @Test
    public void registrationPositiveTest() {
        AuthData authToken = Registration.register("jamil", "corben.wilkie55@gmail.com", "12345");

        assert AuthDataDAO.containsToken(authToken.authToken());
        assert AuthDataDAO.containsUsername("jamil");
    }

    @Test
    public void registrationNegativeTest() {
        Registration.register("jamil", "12345", "corben.wilkie55@gmail.com");

        // register same username twice
        assertThrows(AlreadyTakenException.class, () -> Registration.register("jamil", "12345", "corben.wilkie55@gmail.com"));
    }

    @Test
    public void loginPositiveTest() {
        Registration.register("jamil", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("jamil", "12345");

        assert AuthDataDAO.containsToken(authToken.authToken());
        assert AuthDataDAO.containsUsername("jamil");
    }

    @Test
    public void loginNegativeTest() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        // incorrect password
        assertThrows(NotAuthorizedException.class, () -> Login.login("wilkiec", "123"));
    }

    @Test
    public void loginNegativeTest2() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        // incorrect password
        assertThrows(BadRequestException.class, () -> Login.login("", "123"));
    }

    @Test
    public void logoutPositiveTest() {
        AuthData authToken = Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        Logout.logout(authToken.authToken());

        assert AuthDataDAO.isEmpty();
        assert UserDataDAO.exists("wilkiec");
    }

    @Test
    public void logoutNegativeTest() {
        AuthData authData = Registration.register("jamil", "12345", "corben.wilkie55@gmail.com");

        // logout twice
        Logout.logout(authData.authToken());
        assertThrows(NotAuthorizedException.class, () -> Logout.logout(authData.authToken()));
    }

    @Test
    public void logoutNegativeTest2() {
        // logout without ever being logged in.
        assertThrows(NotAuthorizedException.class, () -> Logout.logout("Not Authorized"));
    }

    @Test
    public void listGamesPositiveTest() {
        Registration.register("jamil", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("jamil", "12345");
        Games.makeGame("myGames", authToken.authToken());
        Games.makeGame("myGameOther", authToken.authToken());
        Games.makeGame("GameNum3", authToken.authToken());
        List<GameDataList> listOfGames = Games.getGames(authToken.authToken());

        assert listOfGames.size() == 3;
        assert listOfGames.getFirst().gameName().equals("myGames");
        assert listOfGames.getLast().gameName().equals("GameNum3");
    }

    @Test
    public void listGamesNegativeTest() {
        // trying to list games without a valid authToken
        assertThrows(NotAuthorizedException.class, () -> Games.getGames("This+ is wrong yo"));
    }

    @Test
    public void listGamesNegativeTest2() {
        AuthData authData = Registration.register("George", "123456", "corn@gmail.com");
        Logout.logout(authData.authToken());

        // trying to list games with a logged out auth Token
        assertThrows(NotAuthorizedException.class, () -> Games.getGames(authData.authToken()));
    }

    @Test
    public void makeGamePositiveTest() {
        Registration.register("Jamil", "12345", "corben.wilkie55@gmail.com");

        // logging in
        AuthData authToken = Login.login("Jamil", "12345");

        // creating a game
        Games.makeGame("TestGame!", authToken.authToken());

        // testing that the game is listed.
        List<GameDataList> listOfGames = Games.getGames(authToken.authToken());
        assert listOfGames.getFirst().gameName().equals("TestGame!");
    }

    @Test
    public void makeGameNegativeTest() {
        // trying to make a game with no game name
        AuthData authToken = Registration.register("Jamil", "12345", "corben.wilkie55@gmail.com");
        assertThrows(BadRequestException.class, () -> Games.makeGame(null, authToken.authToken()));
    }

    @Test
    public void makeGameNegativeTest2() {
        // trying to make a game without a valid authToken
        assertThrows(NotAuthorizedException.class, () -> Games.makeGame("This game doesn't exist", "this_token_is_wrong"));
    }

    @Test
    public void joinGamePositiveTest() {
        Registration.register("Jamil", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("Jamil", "12345");
        int gameID = Games.makeGame("TestGame!", authToken.authToken());

        Games.joinGame(gameID, ChessGame.TeamColor.BLACK, authToken.authToken());

        List<GameDataList> listOfGames = Games.getGames(authToken.authToken());
        assert listOfGames.getFirst().blackUsername().equals("Jamil");
    }

    @Test
    public void joinGameNegativeTest() {
        Registration.register("James", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("James", "12345");
        int gameID = Games.makeGame("TestGame!", authToken.authToken());

        Games.joinGame(gameID, ChessGame.TeamColor.BLACK, authToken.authToken());

        AuthData enemy = Registration.register("myOpp", "12345", "charles@gmail.com");

        assertThrows(AlreadyTakenException.class, () -> Games.joinGame(gameID, ChessGame.TeamColor.BLACK, enemy.authToken()));
    }

    @Test
    public void joinGameNegativeTest2() {
        assertThrows(NotAuthorizedException.class, () -> Games.joinGame(67676767, ChessGame.TeamColor.WHITE, "enemy.authToken()"));
    }
}
