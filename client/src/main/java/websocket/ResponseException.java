package websocket;

public class ResponseException extends Throwable {
    public final String Message;

    public ResponseException(String message) {
        Message = message;
    }
    public String getMessage() {
        return Message;
    }
}
