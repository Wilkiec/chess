package client;

public class ResponseException extends Throwable {
    public final String message;

    public ResponseException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
