package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.infraestructure.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthenticationStrategyFactoryImpl implements AuthenticationStrategyFactory {

    private final Map<String, AuthenticationStrategy> strategies;

    public AuthenticationStrategyFactoryImpl(Map<String, AuthenticationStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public AuthenticationStrategy getStrategy(String type) {
        AuthenticationStrategy strategy = strategies.get(type.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown authentication type: " + type);
        }
        return strategy;
    }
}
