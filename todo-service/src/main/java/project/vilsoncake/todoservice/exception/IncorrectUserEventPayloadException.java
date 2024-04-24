package project.vilsoncake.todoservice.exception;

public class IncorrectUserEventPayloadException extends RuntimeException {
    public IncorrectUserEventPayloadException(String message) {
        super(message);
    }
}
