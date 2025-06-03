package Camaras.VIDEOCAMARAS.infraestructure.strategy;
import Camaras.VIDEOCAMARAS.domain.model.User;

public interface UserDetailsLookupStrategy {
    User lookup(String input);
}