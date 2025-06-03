package Camaras.VIDEOCAMARAS.shared.exceptions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorObject {
    private final Integer statusCode;
    private final String message;
    private final String timestamp;

    public static ErrorObject of(Integer statusCode, String message) {
        return ErrorObject.builder()
                .statusCode(statusCode)
                .message(message)
                .timestamp(java.time.Instant.now().toString())
                .build();
    }
}
