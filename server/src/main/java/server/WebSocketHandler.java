package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDataDAO;
import dataaccess.BadRequestException;
import dataaccess.GameDataDAO;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private static class Connection {
        public String authToken;
        public Session session;
        int gameId;

        public Connection(String authToken, Session session, int gameId) {
            this.authToken = authToken;
            this.session = session;
            this.gameId = gameId;
        }

        public void send(String message) throws IOException {
            session.getRemote().sendString(message);
        }
    }

    private static class ConnectionManager {
        public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

        public void addConnection(String authToken, Session session, int gameId) {
            connections.put(authToken, new Connection(authToken, session, gameId));
        }

        public void removeConnection(String authToken) {
            connections.remove(authToken);
        }

        public void sendToAllClients(String authToken, ServerMessage message, int gameId) throws IOException {
            for (Connection connection : connections.values()) {
                if (connection.session.isOpen()) {
                    if (connection.gameId == gameId) {
                        if (connection.authToken!= null && !connection.authToken.equals(authToken)) {
                            connection.send(new Gson().toJson(message));
                        }
                    }
                }
            }
        }
    }

    private final ConnectionManager connections = new ConnectionManager();

    public void onConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
    }

    public void onError(WsErrorContext ctx) {
        System.out.println("Error: " + ctx.error());
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand message = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            AuthData auth = AuthDataDAO.getAuthData(message.getAuthToken());
            if (auth == null) {
                throw new BadRequestException("Error: unauthorized");
            }
            if (!AuthDataDAO.containsToken(auth.authToken())) {
                throw new BadRequestException("Unauthorized");
            }
            switch (message.getCommandType()) {
                case CONNECT -> connect(message.getGameID(), auth, ctx);
                case LEAVE -> leave(message.getGameID(), auth);
                case RESIGN -> resign(message.getGameID(), auth);
                case MAKE_MOVE -> makeMove(message.getGameID(), message.getChessMove(), auth);
            }
        } catch (Exception e) {
            ctx.send(new Gson().toJson(new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage())));
        }
    }

    private void makeMove(int gameId, ChessMove move, AuthData authData) throws IOException {
        // game updates to represent new move
        GameData game = GameDataDAO.getGame(gameId);

        if (game == null || game.game() == null) {
            throw new BadRequestException("Game Does Not Exist");
        }

        if (game.gameOver()) {
            String won = "Black won";
            if (game.whiteWon()) {won = "White won"; }
            throw new BadRequestException("Game is over. " + won);
        }

        ChessGame.TeamColor turn = game.game().getTeamTurn();
        boolean white = authData.username().equals(game.whiteUsername());
        boolean black = authData.username().equals(game.blackUsername());

        if (!white && !black) {
            throw new BadRequestException("Observers cannot make any moves");
        }
        if (white ^ turn == ChessGame.TeamColor.WHITE) {
            throw new BadRequestException("Not your turn");
        }
        if (white ^ game.game().getBoard().getPiece(move.getStartPosition()).getTeamColor() == ChessGame.TeamColor.WHITE) {
            throw new BadRequestException("Can only move your own pieces");
        }

        try {
            game.game().makeMove(move);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }

        String updateOnGame = authData.username() + " made the move: " + move;
        boolean gameOver = false;
        boolean whiteWon = false;
        if (game.game().isInCheck(ChessGame.TeamColor.WHITE)) {
            updateOnGame += "\nWhite is in check";
        } else if (game.game().isInCheck(ChessGame.TeamColor.BLACK)) {
            updateOnGame += "\nBlack is in check";
        } else if (game.game().isInCheckmate(ChessGame.TeamColor.WHITE)) {
            updateOnGame += "\nWhite is in checkmate";
            gameOver = true;
        } else if (game.game().isInCheckmate(ChessGame.TeamColor.BLACK)) {
            updateOnGame += "\nBlack is in checkmate";
            gameOver = true;
            whiteWon = true;
        } else if (game.game().isInStalemate(ChessGame.TeamColor.WHITE) || game.game().isInStalemate(ChessGame.TeamColor.BLACK)) {
            updateOnGame += "\nStalemate occurred. Game is a draw";
            gameOver = true;
        }

        GameData updatedGameData = new GameData(gameId, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game(), gameOver, whiteWon);

        GameDataDAO.updateGame(gameId, updatedGameData);

        // server sends a load_game message to all clients in the game with an updated game
        LoadGameMessage message = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGameData.game());
        connections.sendToAllClients("", message, gameId);

        // server sends a notification message to all clients in the game informing them what move was made
        // if move results in check, checkmate, or stalemate. server notifies all clients
        NotificationMessage noti = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, updateOnGame);
        connections.sendToAllClients(authData.authToken(), noti, gameId);
    }

    private void connect(int gameId, AuthData auth, WsMessageContext ctx) {
        GameData gameData = GameDataDAO.getGame(gameId);

        if (gameData == null || gameData.game() == null) {
            ctx.send(new Gson().toJson(new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "game does not exist")));
            return;
        }

        connections.addConnection(auth.authToken(), ctx.session, gameId);

        LoadGameMessage message = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        ctx.send(new Gson().toJson(message));

        NotificationMessage noti = getNotificationMessage(auth, gameData);
        try {
            connections.sendToAllClients(auth.authToken(), noti, gameId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static NotificationMessage getNotificationMessage(AuthData auth, GameData gameData) {
        String userHasJoined = auth.username();
        if (auth.username().equals(gameData.whiteUsername())) {
            userHasJoined += " has joined the game as White";
        } else if (auth.username().equals(gameData.blackUsername())) {
            userHasJoined += " has joined the game as Black";
        } else {
            userHasJoined += " has joined the game as an Observer";
        }
        return new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, userHasJoined);
    }

    private void leave(int gameId, AuthData auth) {
        // game is updated. Removes user from game in database
        GameData gameData = GameDataDAO.getGame(gameId);

        if (gameData == null || gameData.game() == null) {
            throw new BadRequestException("Game Does Not Exist");
        }

        if (auth.username().equals(gameData.blackUsername())) {
            GameDataDAO.removePlayer(false, gameId);
        } else if (auth.username().equals(gameData.whiteUsername())) {
            GameDataDAO.removePlayer(true, gameId);
        }

        // server sends  a notification message to all other clients that root left.
        String message = auth.username() + " has left the game";
        NotificationMessage noti = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        try {
            connections.sendToAllClients(auth.authToken(), noti, gameId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // take client out of connections
        connections.removeConnection(auth.authToken());
    }

    private void resign(int gameId, AuthData auth) {
        // server marks the game as complete (no more moves can be completed)
        GameData gameData = GameDataDAO.getGame(gameId);
        if (gameData == null || gameData.game() == null) {
            throw new BadRequestException("Game Does Not Exist");
        }

        boolean white = auth.username().equals(gameData.whiteUsername());
        boolean black = auth.username().equals(gameData.blackUsername());

        if (!white && !black) {
            throw new BadRequestException("Observers cannot resign");
        }

        if (gameData.gameOver()) {
            String thisColorWon = "White won";
            if (!gameData.whiteWon()) {
                thisColorWon = "Black won";
            }
            throw new BadRequestException("Game is already over. " + thisColorWon);
        }

        String won;
        if (black) {
            GameDataDAO.resign(gameId, gameData, true);
            won = "and White has won";
        } else {
            GameDataDAO.resign(gameId, gameData, false);
            won = "and Black has won";
        }
        // server sends a notification message to all clients in the game informing that root resigned
        String message = auth.username() + " has resigned" + won;
        NotificationMessage noti = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        try {
            connections.sendToAllClients("", noti, gameId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
