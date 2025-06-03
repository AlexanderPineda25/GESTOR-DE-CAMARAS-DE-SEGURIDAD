package Camaras.VIDEOCAMARAS.infraestructure.mapper;

import Camaras.VIDEOCAMARAS.domain.model.Rol;
import Camaras.VIDEOCAMARAS.shared.dto.RolDto;

public class RolMapper {
    private RolMapper() {}
    public static RolDto toDto(Rol rol) {
        if (rol == null) return null;
        return RolDto.builder()
                .id(rol.getId())
                .name(rol.getName() != null ? rol.getName().toString() : null)
                .build();
    }
}

