package Camaras.VIDEOCAMARAS.domain.repository;

import Camaras.VIDEOCAMARAS.domain.model.Rol;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByName(RoleType name); // Si usas RoleType enum
}
