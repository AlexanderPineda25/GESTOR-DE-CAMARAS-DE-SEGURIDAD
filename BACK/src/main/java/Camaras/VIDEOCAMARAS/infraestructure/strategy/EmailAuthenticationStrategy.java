package Camaras.VIDEOCAMARAS.infraestructure.strategy;

import Camaras.VIDEOCAMARAS.shared.dto.LoginDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("email")
public class EmailAuthenticationStrategy implements AuthenticationStrategy {

    @Override
    public Authentication authenticate(LoginDto loginDto, AuthenticationManager manager) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());
        return manager.authenticate(authToken);
    }
}
