package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@ClientEndpoint
public class WebSocketFacade {
    public Session session;

    public WebSocketFacade(String serverUrl, ServerMessageObserver observer) throws URISyntaxException, DeploymentException, IOException {
        String urlServer = serverUrl.replace("http", "ws");

        URI socketURI = new URI(urlServer + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, socketURI);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);

                switch (serverMessage.getServerMessageType()) {
                    case LOAD_GAME -> {
                        LoadGameMessage loadGameMessage = new Gson().fromJson(message, LoadGameMessage.class);
                        observer.notify(loadGameMessage);
                    }
                    case ERROR -> {
                        ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
                        observer.notify(errorMessage);
                    }
                    case NOTIFICATION -> {
                        NotificationMessage notificationMessage = new Gson().fromJson(message, NotificationMessage.class);
                        observer.notify(notificationMessage);
                    }
                }
            }
        });
    }

    public void leaveSession(int gameId, String authToken) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId, null);

            String message = new Gson().toJson(command);

            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeMove(String authToken, int gameId, ChessMove move) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId, move);

            String message = new Gson().toJson(command);

            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void joinPlayer(String authToken, int gameId) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId, null);

            String message = new Gson().toJson(command);

            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resignGame(int gameId, String authToken) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId, null);

            String message = new Gson().toJson(command);

            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
