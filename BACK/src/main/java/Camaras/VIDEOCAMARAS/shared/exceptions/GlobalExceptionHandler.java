package Camaras.VIDEOCAMARAS.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorObject> handleNotFoundException(NotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorObject> handleConflictException(ConflictException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorObject> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorObject> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorObject> handleGenericException(Exception ex) {
        ex.printStackTrace(); // o usa un logger
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno inesperado");
    }


    private ResponseEntity<ErrorObject> buildErrorResponse(HttpStatus status, String message) {
        ErrorObject error = ErrorObject.builder()
                .statusCode(status.value())
                .message(message)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.status(status).body(error);
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorObject> handleForbiddenException(ForbiddenException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
