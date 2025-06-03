package Camaras.VIDEOCAMARAS.infraestructure.strategy;

import Camaras.VIDEOCAMARAS.shared.dto.LoginDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;

public interface AuthenticationStrategy {
    Authentication authenticate(LoginDto loginDto, AuthenticationManager manager);
}
