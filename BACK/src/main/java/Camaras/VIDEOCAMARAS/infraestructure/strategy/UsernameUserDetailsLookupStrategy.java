package Camaras.VIDEOCAMARAS.infraestructure.strategy;

import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component("usernameLookupStrategy")
public class UsernameUserDetailsLookupStrategy implements UserDetailsLookupStrategy {

    private final UserRepository userRepository;

    public UsernameUserDetailsLookupStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User lookup(String input) {
        return userRepository.findByUsername(input).orElse(null);
    }
}