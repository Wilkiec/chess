package server;

import com.google.gson.Gson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import org.eclipse.jetty.server.Authentication;
import websocket.commands.UserGameCommand;

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
        } finally {

        }
        ;
    }
}
