package Camaras.VIDEOCAMARAS.domain.repository;

import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    List<User> findByRole_Name(RoleType name);
}
