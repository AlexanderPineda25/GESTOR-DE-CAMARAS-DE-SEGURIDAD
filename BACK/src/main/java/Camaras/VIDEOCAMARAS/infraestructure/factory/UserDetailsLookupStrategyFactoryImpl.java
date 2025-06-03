package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.infraestructure.strategy.EmailUserDetailsLookupStrategy;
import Camaras.VIDEOCAMARAS.infraestructure.strategy.UserDetailsLookupStrategy;
import Camaras.VIDEOCAMARAS.infraestructure.strategy.UsernameUserDetailsLookupStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class UserDetailsLookupStrategyFactoryImpl implements UserDetailsLookupStrategyFactory {
    private final Map<UserLookupType, UserDetailsLookupStrategy> strategies;

    public UserDetailsLookupStrategyFactoryImpl(List<UserDetailsLookupStrategy> strategyList) {
        this.strategies = new EnumMap<>(UserLookupType.class);
        for (UserDetailsLookupStrategy strategy : strategyList) {
            if (strategy instanceof EmailUserDetailsLookupStrategy) {
                strategies.put(UserLookupType.EMAIL, strategy);
            } else if (strategy instanceof UsernameUserDetailsLookupStrategy) {
                strategies.put(UserLookupType.USERNAME, strategy);
            }
        }
    }

    @Override
    public User lookup(UserLookupType type, String input) {
        UserDetailsLookupStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy registered for: " + type);
        }
        return strategy.lookup(input);
    }
}

