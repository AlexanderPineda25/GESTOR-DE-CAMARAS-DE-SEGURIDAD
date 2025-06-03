package Camaras.VIDEOCAMARAS.infraestructure.controller.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class VerifyTokenController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/auth/verify-token")
    public ResponseEntity<String> token() {
        return ResponseEntity.ok("Token válido");
    }
}
