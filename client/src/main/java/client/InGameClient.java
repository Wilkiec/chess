package client;

import chess.ChessMove;
import chess.ChessPosition;
import jakarta.websocket.DeploymentException;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class InGameClient implements ReplClient, ServerMessageObserver {
    private final Repl repl;
    private final String serverUrl;
    private WebSocketFacade ws;

    public InGameClient(Repl repl) {
        this.repl = repl;
        this.serverUrl = repl.serverUrl;
    }

    public String eval(String line) {
        try {
            var tokens = line.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "leave" -> leave(params);
                case "redraw" -> redraw(params);
                case "highlight" -> highlightLegalMoves(params);
                case "resign" -> resign(params);
                case "move" -> makeMove(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String makeMove(String[] params) {
        // parse user input:
        if (params.length != 2) {
            return "please include start and end position";
        }

        try {
            ChessPosition startPos = moveParse(params[0]);
            ChessPosition endPos = moveParse(params[1]);

            // how to tell if user needs to specify promotion piece?

            ChessMove move = new ChessMove(startPos, endPos, null);

            WebSocketFacade.makeMove(repl.authToken, repl.gameId, move);

            return "move sent";
        } catch (Exception | ResponseException e) {
            return "please use a valid start and end position. Ex: move a2 a4";
        }
    }

    private String resign(String[] params) {
        return "";
    }

    private String highlightLegalMoves(String[] params) {
        return "";
    }

    private String redraw(String[] params) {
        return "";
    }

    public String leave(String[] params) throws ResponseException {
        if (params.length != 0) {
            return "leave Command Takes No Inputs";
        }

        repl.setLoggedIn(repl.authToken);
        return "successfully left game";
    }

    public String help() throws ResponseException {
        return """
                highlight <Piece Position> - highlight all legal moves for a given piece
                move <Start Position> <End Position> - make a move!
                resign - forfeit the chess match
                leave - go back to menu (doesn't forfeit the game)
                quit - Exit application
                help - Learn the commands
                """;
    }

    ChessPosition moveParse(String position) throws ResponseException {
        if (position.length() != 2) {
            throw new ResponseException("invalid move");
        }

        char colChar = Character.toLowerCase(position.charAt(0));
        int col = colChar - 'a' + 1;

        int row = Character.getNumericValue(position.charAt(1));

        if (row < 0 || row > 8 || col < 0 || col > 8) {
            throw new ResponseException("invalid move");
        }

        return new ChessPosition(row, col);
    }

    public void enter() {
        try {
            ws = new WebSocketFacade(serverUrl, this);

            ws.connect(repl.authToken, repl.gameId);

        } catch (DeploymentException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notify(ServerMessage message) {

    }
}
