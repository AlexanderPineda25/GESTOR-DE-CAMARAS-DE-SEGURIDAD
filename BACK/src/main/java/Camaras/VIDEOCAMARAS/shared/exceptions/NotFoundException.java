package Camaras.VIDEOCAMARAS.shared.exceptions;

public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
