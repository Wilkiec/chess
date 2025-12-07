package websocket.messages;

public class ErrorMessage extends ServerMessage{
    private final String message;
    public ErrorMessage(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String getErrorMessage() {
        return message;
    }
}
