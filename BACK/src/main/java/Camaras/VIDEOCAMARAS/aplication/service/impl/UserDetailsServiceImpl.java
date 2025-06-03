package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.infraestructure.factory.UserDetailsLookupStrategyFactory;
import Camaras.VIDEOCAMARAS.infraestructure.factory.UserLookupType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service("userDetailsService")
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDetailsLookupStrategyFactory strategyFactory;

    public UserDetailsServiceImpl(UserDetailsLookupStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        // Determina si es email o username (ajusta esta lógica a tu necesidad real)
        UserLookupType lookupType = input.contains("@") ? UserLookupType.EMAIL : UserLookupType.USERNAME;

        User user = strategyFactory.lookup(lookupType, input);
        if (user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + input);
        }

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getName());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Set.of(authority)
        );
    }
}
