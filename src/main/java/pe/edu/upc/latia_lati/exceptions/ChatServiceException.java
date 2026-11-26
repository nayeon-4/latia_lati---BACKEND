package pe.edu.upc.latia_lati.exceptions;

import org.springframework.http.HttpStatus;

public class ChatServiceException extends RuntimeException {
    private final HttpStatus status;

    public ChatServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
