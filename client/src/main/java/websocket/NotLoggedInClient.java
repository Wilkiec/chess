package websocket;

import model.AuthData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class NotLoggedInClient implements ReplClient{
    private final ServerFacade server;
    private final Repl repl;

    public NotLoggedInClient(Repl repl, ServerFacade server) {
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
                case "login" -> login(params);
                case "register" -> register(params);
                default -> help();
            };
        } catch (ResponseException | URISyntaxException | IOException ex) {
            return ex.getMessage();
        }
    }

    public String login(String[] params) throws ResponseException, URISyntaxException, IOException {
        if (params.length != 2) {
            throw new ResponseException("Incorrect Length of login Input");
        }
        String username = params[0];
        String password = params[1];

        AuthData authData = server.login(username, password);

        repl.setLoggedIn(authData.authToken());

        return "Hello " + authData.username();
    }

    public String register(String[] params) throws ResponseException {
        if (params.length != 3) {
            throw new ResponseException("Register Command requires 3 Inputs");
        }
        String username = params[0];
        String password = params[1];
        String email = params[2];

        AuthData authData = server.register(username, password, email);

        repl.setLoggedIn(authData.authToken());

        return "Hello " + authData.username();
    }

    public String help() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - Create an account
                login <USERNAME> <PASSWORD> - Login to your account
                quit - Exit Chess Application
                help - Learn the commands
                """;
    }

}
