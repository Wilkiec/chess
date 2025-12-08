package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.*;
import service.*;

import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();

    private static class ErrorResponse {
        String message;
        public ErrorResponse(String message) {
            this.message = "Error: " + message;
        }
    }

    private void sendJsonResponse(Context ctx, Object data, int statusCode) {
        ctx.status(statusCode);
        ctx.result(gson.toJson(data));
        ctx.contentType("application/json");
    }

    public Server() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ignored) {
        }

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        WebSocketHandler wsHandler = new WebSocketHandler();
        javalin.ws("/ws", ws -> {
            ws.onConnect(wsHandler::onConnect);
            ws.onMessage(wsHandler::onMessage);
            ws.onError(wsHandler::onError);
        });

        this.javalin.delete("/db", ctx -> {
            try {
                Clear.clearApp();
                ctx.status(200);
            } catch (Exception | DataAccessException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);}
        });

        javalin.post("/user", ctx -> {
            try {
                RegReq request = new Gson().fromJson(ctx.body(), RegReq.class);
                AuthData authDat = Registration.register(request.username(), request.password(), request.email());
                ctx.status(200);
                Gson gson = new Gson();
                String jsonString = gson.toJson(authDat);
                ctx.result(jsonString);
                ctx.contentType("application/json");
            } catch (AlreadyTakenException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 403);}
            catch (BadRequestException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 400);}
            catch (Exception e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);}
        });

        javalin.post("/session", ctx -> {
            try {
                LoginRequired userInf = new Gson().fromJson(ctx.body(), LoginRequired.class);
                AuthData authDat = Login.login(userInf.username(), userInf.password());
                ctx.status(200);
                Gson gson = new Gson();
                String jsonString = gson.toJson(authDat);
                ctx.result(jsonString);
                ctx.contentType("application/json");
            } catch (BadRequestException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 400);}
            catch (NotAuthorizedException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);}
            catch (Exception e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);}
        });

        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                Logout.logout(authToken);
                ctx.status(200);
            } catch (NotAuthorizedException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);}
            catch (Exception e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);}
        });

        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                List<GameDataList> game = Games.getGames(authToken);

                ctx.status(200);
                GameListResponse response = new GameListResponse(game);
                Gson gson = new Gson();
                String jString = gson.toJson(response);
                ctx.result(jString);
                ctx.contentType("application/json");
            } catch (NotAuthorizedException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);}
            catch (Exception e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);}
        });

        javalin.post("/game", ctx -> {
            try {
                GameMakeRequired gameInfo = new Gson().fromJson(ctx.body(), GameMakeRequired.class);
                String authToken = ctx.header("authorization");
                int gameID = Games.makeGame(gameInfo.gameName(), authToken);
                ctx.status(200);
                Gson gson = new Gson();
                String jString = gson.toJson(Map.of("gameID", String.valueOf(gameID)));
                ctx.result(jString);
                ctx.contentType("application/json");
            } catch (BadRequestException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 400);}
            catch (NotAuthorizedException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);}
            catch (Exception e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);}
        });

        javalin.put("/game", ctx -> {
            try {
                GameJoinRequired gameInfo = new Gson().fromJson(ctx.body(), GameJoinRequired.class);
                String authToken = ctx.header("authorization");

                Games.joinGame(gameInfo.gameID(), gameInfo.playerColor(), authToken);
                ctx.status(200);
            } catch (BadRequestException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 400);}
            catch (NotAuthorizedException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);}
            catch (AlreadyTakenException e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 403);}
            catch (Exception e) {sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);}
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
