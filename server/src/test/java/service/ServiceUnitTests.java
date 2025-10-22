package service;

import chess.ChessGame;
import dataaccess.*;
import model.authData;
import model.gameDataList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServiceUnitTests {
    @BeforeEach
    public void clearData() {
        clear.clearApp();
    }

    @Test
    public void clearPositiveServiceTest() {
        registration.register("wilkiec", "corben.wilkie55@gmail.com", "12345");
        clear.clearApp();

        assert userDataDAO.isEmpty();
        assert authDataDAO.isEmpty();
        assert gameDataDAO.isEmpty();
    }

    @Test
    public void registrationPositiveServiceTest() {
        authData authToken = registration.register("wilkiec", "corben.wilkie55@gmail.com", "12345");

        assert authDataDAO.containsToken(authToken.authToken());
        assert authDataDAO.containsUsername("wilkiec");
    }

    @Test
    public void registrationNegativeServiceTest() {
        registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        // register same username twice
        assertThrows(AlreadyTakenException.class, () -> registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com"));
    }

    @Test
    public void loginPositiveServiceTest() {
        registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        authData authToken = Login.login("wilkiec", "12345");

        assert authDataDAO.containsToken(authToken.authToken());
        assert authDataDAO.containsUsername("wilkiec");
    }

    @Test
    public void loginNegativeServiceTest() {
        registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        // incorrect username
        assertThrows(NotAuthorizedException.class, () -> Login.login("wilkie", "12345"));
    }

    @Test
    public void logoutPositiveServiceTest() {
        authData authToken = registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        Logout.logout(authToken.authToken());

        assert authDataDAO.isEmpty();
        assert userDataDAO.exists("wilkiec");
    }

    @Test
    public void logoutNegativeServiceTest() {
        authData authData = registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        // logout twice
        Logout.logout(authData.authToken());
        assertThrows(NotAuthorizedException.class, () -> Logout.logout(authData.authToken()));
    }

    @Test
    public void listGamesPositiveServiceTest() {
        registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        authData authToken = Login.login("wilkiec", "12345");
        games.makeGame("myGame", authToken.authToken());
        games.makeGame("GameNum2", authToken.authToken());
        List<gameDataList> listOfGames = games.getGames(authToken.authToken());

        assert listOfGames.size() == 2;
        assert listOfGames.getFirst().gameName().equals("myGame");
        assert listOfGames.getLast().gameName().equals("GameNum2");
    }

    @Test
    public void listGamesNegativeServiceTest() {
        // trying to list games without a valid authToken
        assertThrows(NotAuthorizedException.class, () -> games.getGames("this_token_is_wrong"));
    }

    @Test
    public void makeGamePositiveServiceTest() {
        registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        authData authToken = Login.login("wilkiec", "12345");
        games.makeGame("TestGame!", authToken.authToken());
        List<gameDataList> listOfGames = games.getGames(authToken.authToken());
        assert listOfGames.getFirst().gameName().equals("TestGame!");
    }

    @Test
    public void makeGameNegativeServiceTest() {
        // trying to make a game without a valid authToken
        assertThrows(NotAuthorizedException.class, () -> games.makeGame("game1", "this_token_is_wrong"));

        // trying to make a game with no game name
        authData authToken = registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");
        assertThrows(BadRequestException.class, () -> games.makeGame(null, authToken.authToken()));
    }

    @Test
    public void joinGamePositiveServiceTest() {
        registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        authData authToken = Login.login("wilkiec", "12345");
        int gameID = games.makeGame("TestGame!", authToken.authToken());

        games.joinGame(gameID, ChessGame.TeamColor.WHITE, authToken.authToken());

        List<gameDataList> listOfGames = games.getGames(authToken.authToken());
        assert listOfGames.getFirst().whiteUsername().equals("wilkiec");
    }

    @Test
    public void joinGameNegativeServiceTest() {
        registration.register("wilkiec", "12345", "corben.wilkie55@gmail.com");

        authData authToken = Login.login("wilkiec", "12345");
        int gameID = games.makeGame("TestGame!", authToken.authToken());

        games.joinGame(gameID, ChessGame.TeamColor.WHITE, authToken.authToken());

        authData enemy = registration.register("myOpp", "12345", "charles@gmail.com");

        assertThrows(AlreadyTakenException.class, () -> games.joinGame(gameID, ChessGame.TeamColor.WHITE, enemy.authToken()));
    }
}
