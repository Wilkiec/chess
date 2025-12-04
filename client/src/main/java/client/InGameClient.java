package client;

import java.util.Arrays;

public class InGameClient implements ReplClient {
    private final Repl repl;

    public InGameClient(Repl repl) {
        this.repl = repl;
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
        return "";
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


}
