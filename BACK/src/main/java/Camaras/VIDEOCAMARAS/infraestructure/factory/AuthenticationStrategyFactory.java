package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.infraestructure.strategy.AuthenticationStrategy;

public interface AuthenticationStrategyFactory {
    AuthenticationStrategy getStrategy(String type);
}
