package Camaras.VIDEOCAMARAS.shared.exceptions;

public class ConflictException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
