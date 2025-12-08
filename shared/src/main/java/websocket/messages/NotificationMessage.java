package websocket.messages;

public class NotificationMessage extends ServerMessage{
    String message;
    public NotificationMessage(ServerMessageType type, String json) {
        super(type);
        this.message = json;
    }

    public String getMessage() {return message; }
}
