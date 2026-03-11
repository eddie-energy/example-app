package energy.eddie.exampleapp.exception;

public class WebSocketAuthenticationException extends RuntimeException {
    public WebSocketAuthenticationException(String message) {
        super(message);
    }
}
