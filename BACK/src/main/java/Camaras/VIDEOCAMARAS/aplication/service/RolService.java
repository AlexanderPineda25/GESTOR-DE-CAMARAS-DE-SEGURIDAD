package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.domain.model.Rol;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import java.util.List;
import java.util.Optional;

public interface RolService {
    Optional<Rol> findByName(RoleType roleType);
    Rol createRoleIfNotExist(RoleType roleType);
    List<Rol> findAll();
}
