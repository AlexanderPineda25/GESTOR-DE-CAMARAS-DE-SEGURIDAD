package Camaras.VIDEOCAMARAS.infraestructure.controller.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints exclusivos para administradores.
 */
@RestController
public class AdminAccessController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/welcome")
    public String admin() {
        return "¡Hola, bienvenido Admin!";
    }
}
