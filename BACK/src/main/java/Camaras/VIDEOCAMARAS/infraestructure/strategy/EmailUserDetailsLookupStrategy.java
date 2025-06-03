package Camaras.VIDEOCAMARAS.infraestructure.strategy;

import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component("emailLookupStrategy")
public class EmailUserDetailsLookupStrategy implements UserDetailsLookupStrategy {

    private final UserRepository userRepository;

    public EmailUserDetailsLookupStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User lookup(String input) {
        return userRepository.findByEmail(input).orElse(null);
    }
}