package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.domain.model.User;

public interface UserDetailsLookupStrategyFactory {
    User lookup(UserLookupType type, String input);
}
