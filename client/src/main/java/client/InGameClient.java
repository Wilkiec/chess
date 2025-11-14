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
            if (cmd.equals("quit")) {
                return "quit";
            } else if (cmd.equals("back")) {
                return back(params);
            } else {
                return "Invalid Argument";
            }
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String back(String[] params) throws ResponseException {
        if (params.length != 0) {
            return "Back Command Takes No Inputs";
        }
        repl.setLoggedIn(repl.authToken);
        return "successfully went back";
    }
}
