package client;

import chess.ChessMove;
import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade {
    public Session session;

    public WebSocketFacade(String serverUrl, ServerMessageObserver observer) throws URISyntaxException, DeploymentException, IOException {
        URI socketURI = new URI("ws://localhost:8080/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, socketURI);
    }


    public static void makeMove(String authToken, int gameId, ChessMove move) {

    }

    public void connect(String authToken, int gameId) {
    }
}
