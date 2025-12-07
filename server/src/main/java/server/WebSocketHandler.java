package server;

import com.google.gson.Gson;
import com.mysql.cj.jdbc.ConnectionGroupManager;
import dataaccess.AuthDataDAO;
import dataaccess.GameDataDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

import static javax.management.remote.JMXConnectorFactory.connect;

public class WebSocketHandler {
    public void onConnect(WsConnectContext ctx) {
        System.out.println("User Connected");
    }

    public void onClose(WsCloseContext ctx) {
        System.out.println("User disconnected");
    }

    public void onError(WsErrorContext ctx) {
        System.out.println("Error: " + ctx.error());
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand message = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            AuthData auth = AuthDataDAO.generateAuthData(AuthDataDAO.usernameOfAuthToken(message.getAuthToken()));

            switch (message.getCommandType()) {
                case CONNECT -> connect(message.getGameID(), auth, ctx);
                case LEAVE -> {
                    // game is updated. Removes user from game in database
                    // server sends  a notification message to all other clients that root left.
                }
                case RESIGN -> {
                    // server marks the game as complete (no more moves can be completed)
                    // server sends a notification message to all clients in the game informing that root resigned
                }
                case MAKE_MOVE -> {
                    // server verifies validity of move
                    // game updates to represent new move
                    // server sends a load_game message to all clients in the game with an updated game
                    // server sends a notification message to all clients in the game informing them what move was made
                    // if move results in check, checkmate, or stalemate. server notifies all clients
                }
            }
        } catch (Exception e) {
            ctx.send(new Gson().toJson(new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage())));
        }
    }

    private void connect(int gameId, AuthData authData, WsMessageContext ctx) {
        // have some way to make add the data to list of people in said game

        GameData gameData = GameDataDAO.getGame(gameId);

        if (gameData == null || gameData.game() == null) {
            ctx.send(new Gson().toJson(new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "game does not exist")));
        }

        LoadGameMessage message = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());

        ctx.send(new Gson().toJson(message));

        // figure out how to send to everyone else that user joined.
    }
}
