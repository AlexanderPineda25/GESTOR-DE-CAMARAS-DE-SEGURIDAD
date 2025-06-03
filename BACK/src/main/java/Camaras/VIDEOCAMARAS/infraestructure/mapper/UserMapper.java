package Camaras.VIDEOCAMARAS.infraestructure.mapper;

import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.shared.dto.UserDto;
import Camaras.VIDEOCAMARAS.shared.dto.UserResponseDto;

public class UserMapper {

    private UserMapper() {}

    public static UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(RolMapper.toDto(user.getRole()))
                .build();
    }
    public static UserResponseDto toResponseDto(UserDto userDto) {
        if (userDto == null) return null;
        return UserResponseDto.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .build();
    }
}
