package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.domain.model.Rol;
import Camaras.VIDEOCAMARAS.shared.dto.RegisterDto;

public class UserFactory {
    private UserFactory() {}

    public static User create(RegisterDto dto, Rol role, String encodedPassword) {
        if (dto == null) throw new IllegalArgumentException("UserCreateDto no puede ser null");
        if (role == null) throw new IllegalArgumentException("Rol no puede ser null");
        if (encodedPassword == null || encodedPassword.isBlank()) throw new IllegalArgumentException("Password no puede ser null o vacío");

        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(encodedPassword)
                .role(role)
                .build();
    }
}
