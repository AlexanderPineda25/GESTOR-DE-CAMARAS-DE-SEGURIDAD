package Camaras.VIDEOCAMARAS.infraestructure.security;

import Camaras.VIDEOCAMARAS.shared.exceptions.ErrorObject;
import Camaras.VIDEOCAMARAS.shared.exceptions.JwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Throwable exception = (Throwable) request.getAttribute("exception");

        String message = (exception instanceof JwtAuthenticationException)
                ? exception.getMessage()
                : "Acceso no autorizado al recurso";

        ErrorObject error = ErrorObject.of(HttpServletResponse.SC_UNAUTHORIZED, message);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
