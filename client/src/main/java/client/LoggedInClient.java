package client;

import chess.ChessGame;

import java.util.Arrays;

import static client.BoardDrawer.drawBoard;

public class LoggedInClient implements ReplClient {
    private final ServerFacade server;
    private final Repl repl;

    public LoggedInClient(Repl repl, ServerFacade server) {
        this.repl = repl;
        this.server = server;
    }

    public String eval(String line) {
        try {
            var tokens = line.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "quit" -> "quit";
                case "logout" -> logout(params);
                case "create" -> createChessGame(params);
                case "list" -> listGame(params);
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String observeGame(String[] params) {
        if (params.length != 1) {
            return "Observe Command Requires 1 Input";
        }

        String gameId = params[0];

        int gameIdInt;
        try {
            gameIdInt = Integer.parseInt(gameId);
        } catch (NumberFormatException e) {
            return "Please Provide a Valid Integer";
        }

        ChessGame board = new ChessGame();

        drawBoard(board.getBoard(), true);

        repl.setInGame(gameIdInt, false, true);

        return "Successfully Joined Game As Spectator";
    }

    public String joinGame(String[] params) throws ResponseException {
        if (params.length != 2) {
            return "Join Command Requires 2 Inputs";
        }

        int gameId;
        try {
            gameId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Please Provide a Valid Integer";
        }

        String color = params[1];

        server.joinGame(repl.authToken, gameId, color);

        boolean white;
        if (color.equals("white")) {
            white = true;
        } else if (color.equals("black")) {
            white = false;
        } else {
            return "Must Provide A Valid Game Color";
        }

        repl.setInGame(gameId, true, white);

        ChessGame board = new ChessGame();

        drawBoard(board.getBoard(), white);

        return "Successfully joined game " + gameId;
    }

    public String listGame(String[] params) throws ResponseException {
        if (params.length != 0) {
            return "List Command Does Not Take Any Inputs";
        }

        GameListResponse gameList = server.listGame(repl.authToken);

        if (gameList.games() == null || gameList.games().isEmpty()) {
            return "No Games Available Right now";
        }

        int gameNum = 1;
        StringBuilder games = new StringBuilder("Games:\n");

        for (GameData game : gameList.games()) {
            games.append(gameNum++);
            games.append(".  Game Name: ");
            games.append(game.gameName());
            games.append("  White: ");
            if (game.whiteUsername() != null) {
                games.append(game.whiteUsername());
            }
            games.append("  Black: ");
            if (game.blackUsername() != null) {
                games.append(game.blackUsername());
            }
            games.append("\n");
        }

        return games.toString();
    }

    public String logout(String[] params) throws ResponseException {
        if (params.length != 0) {
            throw new ResponseException("Logout takes no inputs");
        }

        server.logout(repl.authToken);

        repl.setLoggedOut();

        return "Successfully logged out";
    }

    public String createChessGame(String[] params) throws ResponseException {
        if (params.length != 1) {
            throw new ResponseException("Incorrect length of Create Command");
        }
        String gameName = params[0];

        server.createGame(repl.authToken, gameName);

        return "Successfully Created " + gameName;
    }

    public String help() throws ResponseException {
        return """
                create <NAME> - Create a new Chess game!
                list - List all the created games
                join <ID> [WHITE|BLACK] - Join a specific game with the given color
                observe <ID> - Watch a Chess game
                logout - Logout of your account
                quit - Exit application
                help - Learn the commands
                """;
    }
}
