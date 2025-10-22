package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.NotAuthorizedException;
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

    private static class GameListResponse {
        Object games;

        public GameListResponse(Object games) {
            this.games = games;
        }
    }

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        this.javalin.delete("/db", ctx -> {
            try {
                clear.clearApp();
                ctx.status(200);
            } catch (Exception e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);
            }
        });

        javalin.post("/user", ctx -> {
            try {
                RegReq request = new Gson().fromJson(ctx.body(), RegReq.class);
                authData authDat = registration.register(request.username(), request.password(), request.email());
                ctx.status(200);
                Gson gson = new Gson();
                String jsonString = gson.toJson(authDat);
                ctx.result(jsonString);
                ctx.contentType("application/json");
            } catch (AlreadyTakenException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 403);
            } catch (BadRequestException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 400);
            } catch (Exception e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);
            }
        });

        javalin.post("/session", ctx -> {
            try {
                LoginRequired userInf = new Gson().fromJson(ctx.body(), LoginRequired.class);
                authData authDat = Login.login(userInf.username(), userInf.password());
                ctx.status(200);
                Gson gson = new Gson();
                String jsonString = gson.toJson(authDat);
                ctx.result(jsonString);
                ctx.contentType("application/json");
            } catch (BadRequestException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 400);
            } catch (NotAuthorizedException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);
            } catch (Exception e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);
            }
        });

        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                Logout.logout(authToken);
                ctx.status(200);
            } catch (NotAuthorizedException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);
            } catch (Exception e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);
            }
        });

        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                List<gameDataList> game = games.getGames(authToken);
                ctx.status(200);
                GameListResponse response = new GameListResponse(game);
                Gson gson = new Gson();
                String jString = gson.toJson(response);
                ctx.result(jString);
                ctx.contentType("application/json");
            } catch (NotAuthorizedException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);
            } catch (Exception e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);
            }
        });

        javalin.post("/game", ctx -> {
            try {
                GameMakeRequired gameInfo = new Gson().fromJson(ctx.body(), GameMakeRequired.class);
                String authToken = ctx.header("authorization");
                int gameID = games.makeGame(gameInfo.gameName(), authToken);
                ctx.status(200);
                Gson gson = new Gson();
                String jString = gson.toJson(Map.of("gameID", String.valueOf(gameID)));
                ctx.result(jString);
                ctx.contentType("application/json");
            } catch (BadRequestException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 400);
            } catch (NotAuthorizedException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);
            } catch (Exception e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);
            }
        });

        javalin.put("/game", ctx -> {
            try {
                GameJoinRequired gameInfo = new Gson().fromJson(ctx.body(), GameJoinRequired.class);
                String authToken = ctx.header("authorization");

                games.joinGame(gameInfo.gameID(), gameInfo.playerColor(), authToken);
                ctx.status(200);
            } catch (BadRequestException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 400);
            } catch (NotAuthorizedException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 401);
            } catch (AlreadyTakenException e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 403);
            } catch (Exception e) {
                sendJsonResponse(ctx, new ErrorResponse(e.getMessage()), 500);
            }
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
