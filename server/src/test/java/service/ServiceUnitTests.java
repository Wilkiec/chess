package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameDataList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServiceUnitTests {
    @BeforeAll
    public static void makeDataBase() throws DataAccessException {
        DatabaseManager.createDatabase();
    }

    @BeforeEach
    public void clearData() throws Exception, DataAccessException {
        Clear.clearApp();
    }

    @Test
    public void clearPositiveServiceTest() throws Exception, DataAccessException {
        Registration.register("wilkiec", "corben.wilkie55@gmail.com", "12345");
        Clear.clearApp();

        assert UserDataDAO.isEmpty();
        assert AuthDataDAO.isEmpty();
        assert GameDataDAO.isEmpty();
    }

    @Test
    public void registrationPositiveServiceTest() {
        AuthData authToken = Registration.register("wilkiec", "corben.wilkie55@gmail.com", "12345");

        assert AuthDataDAO.containsToken(authToken.authToken());
        assert AuthDataDAO.containsUsername("wilkiec");
    }

    @Test
    public void registrationNegativeServiceTest() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        // register same username twice
        assertThrows(AlreadyTakenException.class, () -> Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com"));
    }

    @Test
    public void loginPositiveServiceTest() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("wilkiec", "12345");

        assert AuthDataDAO.containsToken(authToken.authToken());
        assert AuthDataDAO.containsUsername("wilkiec");
    }

    @Test
    public void loginNegativeServiceTest() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        // incorrect username
        assertThrows(NotAuthorizedException.class, () -> Login.login("wilkie", "12345"));
    }

    @Test
    public void logoutPositiveServiceTest() {
        AuthData authToken = Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        Logout.logout(authToken.authToken());

        assert AuthDataDAO.isEmpty();
        assert UserDataDAO.exists("wilkiec");
    }

    @Test
    public void logoutNegativeServiceTest() {
        AuthData authData = Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        // logout twice
        Logout.logout(authData.authToken());
        assertThrows(NotAuthorizedException.class, () -> Logout.logout(authData.authToken()));
    }

    @Test
    public void listGamesPositiveServiceTest() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("wilkiec", "12345");
        Games.makeGame("myGame", authToken.authToken());
        Games.makeGame("GameNum2", authToken.authToken());
        List<GameDataList> listOfGames = Games.getGames(authToken.authToken());

        assert listOfGames.size() == 2;
        assert listOfGames.getFirst().gameName().equals("myGame");
        assert listOfGames.getLast().gameName().equals("GameNum2");
    }

    @Test
    public void listGamesNegativeServiceTest() {
        // trying to list games without a valid authToken
        assertThrows(NotAuthorizedException.class, () -> Games.getGames("this_token_is_wrong"));
    }

    @Test
    public void makeGamePositiveServiceTest() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("wilkiec", "12345");
        Games.makeGame("TestGame!", authToken.authToken());
        List<GameDataList> listOfGames = Games.getGames(authToken.authToken());
        assert listOfGames.getFirst().gameName().equals("TestGame!");
    }

    @Test
    public void makeGameNegativeServiceTest() {
        // trying to make a game without a valid authToken
        assertThrows(NotAuthorizedException.class, () -> Games.makeGame("game1", "this_token_is_wrong"));

        // trying to make a game with no game name
        AuthData authToken = Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        assertThrows(BadRequestException.class, () -> Games.makeGame(null, authToken.authToken()));
    }

    @Test
    public void joinGamePositiveServiceTest() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("wilkiec", "12345");
        int gameID = Games.makeGame("TestGame!", authToken.authToken());

        Games.joinGame(gameID, ChessGame.TeamColor.WHITE, authToken.authToken());

        List<GameDataList> listOfGames = Games.getGames(authToken.authToken());
        assert listOfGames.getFirst().whiteUsername().equals("wilkiec");
    }

    @Test
    public void joinGameNegativeServiceTest() {
        Registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        AuthData authToken = Login.login("wilkiec", "12345");
        int gameID = Games.makeGame("TestGame!", authToken.authToken());

        Games.joinGame(gameID, ChessGame.TeamColor.WHITE, authToken.authToken());

        AuthData enemy = Registration.register("myOpp", "12345", "charles@gmail.com");

        assertThrows(AlreadyTakenException.class, () -> Games.joinGame(gameID, ChessGame.TeamColor.WHITE, enemy.authToken()));
    }
}
