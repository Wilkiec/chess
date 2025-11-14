package client;

import java.util.*;

import static client.BoardDrawer.systemResetColor;

public class Repl {
    private int current = 0;
    private final ArrayList<ReplClient> activeClient;
    int gameId = 0;
    boolean player = false;
    boolean white = true;
    String authToken;

    public Repl(String serverUrl) {
        ServerFacade server = new ServerFacade(serverUrl);

        NotLoggedInClient client0 = new NotLoggedInClient(this, server);
        LoggedInClient client1 = new LoggedInClient(this, server);
        InGameClient client2 = new InGameClient(this);
        this.activeClient = new ArrayList<>(Arrays.asList(client0, client1, client2));
        this.authToken = "";
    }

    public void run() {
        systemResetColor();
        System.out.println("Welcome to my Chess Application");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            String line = scanner.nextLine();

            try {
                result = activeClient.get(current).eval(line);
                System.out.println(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.println(msg);
            }
        }
        System.out.println();
    }

    public void setLoggedOut() {
        this.current = 0;
    }

    public void setLoggedIn(String authToken) {
        this.current = 1;
        this.authToken = authToken;
    }

    public void setInGame(int gameId, Boolean player, Boolean white) {
        this.current = 2;
        this.gameId = gameId;
        this.player = player;
        this.white = white;
    }
}
