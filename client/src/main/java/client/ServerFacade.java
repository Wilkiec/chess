package client;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameJoinRequired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

record ErrorResponse(String message) {}
record LoginRequired(String username, String password) {}
record RegReq(String username, String password, String email) {}
record GameMakeRequired(String gameName) {}
record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {}

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public AuthData login(String username, String password) throws ResponseException {
        try {
            URI uri = new URI(serverUrl + "/session");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);

            http.setRequestProperty("Content-Type", "application/json");

            LoginRequired request = new LoginRequired(username, password);
            String responseJson = new Gson().toJson(request);

            try (OutputStream os = http.getOutputStream()) {
                os.write(responseJson.getBytes(StandardCharsets.UTF_8));
            }

            return readResponse(http, AuthData.class);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        try {
            URI uri = new URI(serverUrl + "/user");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);

            http.setRequestProperty("Content-Type", "application/json");

            RegReq request = new RegReq(username, password, email);
            String responseJson = new Gson().toJson(request);

            try (OutputStream os = http.getOutputStream()) {
                os.write(responseJson.getBytes(StandardCharsets.UTF_8));
            }

            return readResponse(http, AuthData.class);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public void createGame(String authToken, String gameName) throws ResponseException {
        try {
            URI uri = new URI(serverUrl + "/game");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);

            http.setRequestProperty("authorization", authToken);
            GameMakeRequired request = new GameMakeRequired(gameName);
            String responseJson = new Gson().toJson(request);

            try (OutputStream os = http.getOutputStream()) {
                os.write(responseJson.getBytes(StandardCharsets.UTF_8));
            }

            readResponse(http, AuthData.class);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public GameListResponse listGame(String authToken) throws ResponseException {
        try {
            URI uri = new URI(serverUrl + "/game");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();

            http.setRequestMethod("GET");
            http.setRequestProperty("authorization", authToken);

            return readResponse(http, GameListResponse.class);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public void joinGame(String authToken, int gameId, String color) throws ResponseException {
        ChessGame.TeamColor teamColor;
        if (color.equals("white")) {
            teamColor = ChessGame.TeamColor.WHITE;
        } else if (color.equals("black")) {
            teamColor = ChessGame.TeamColor.BLACK;
        } else {
            throw new ResponseException("must Provide a valid TeamColor");
        }

        try {
            URI uri = new URI(serverUrl + "/game");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();

            http.setRequestMethod("PUT");
            http.setDoOutput(true);
            http.setRequestProperty("authorization", authToken);

            GameJoinRequired request = new GameJoinRequired(teamColor, gameId);
            String responseJson = new Gson().toJson(request);

            try (OutputStream os = http.getOutputStream()) {
                os.write(responseJson.getBytes(StandardCharsets.UTF_8));
            }

            readResponse(http, AuthData.class);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public void logout(String authToken) throws ResponseException {
        try {
            URI uri = new URI(serverUrl + "/session");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();

            http.setRequestMethod("DELETE");
            http.setRequestProperty("authorization", authToken);

            readResponse(http, null);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public <T> T readResponse(HttpURLConnection http, Class<T> responseClass) throws IOException, ResponseException {
        int statusCode = http.getResponseCode();
        String responseBody;

        if (statusCode == 200) {
            try (InputStream is = http.getInputStream()) {
                responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (responseClass == null) {
                return null;
            }
            return new Gson().fromJson(responseBody, responseClass);
        } else {
            try (InputStream errorMessage = http.getErrorStream()) {
                responseBody = new String(errorMessage.readAllBytes(), StandardCharsets.UTF_8);
            }
            ErrorResponse errorMessage = new Gson().fromJson(responseBody, ErrorResponse.class);
            throw new ResponseException(errorMessage.message());
        }
    }
}
