package Camaras.VIDEOCAMARAS.infraestructure.controller.auth;

import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de bienvenida exclusivo para usuarios con el rol USER.
 */
@RestController
public class UserAccessController {

    @Transactional // Añade esta anotación
    @GetMapping("/api/users/welcome")
    @PreAuthorize("hasRole('USER')")
    public String user() {
        return "¡Bienvenido User!";
    }
}
