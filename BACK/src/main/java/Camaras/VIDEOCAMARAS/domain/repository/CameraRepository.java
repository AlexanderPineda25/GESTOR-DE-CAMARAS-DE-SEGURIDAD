package Camaras.VIDEOCAMARAS.domain.repository;

import Camaras.VIDEOCAMARAS.domain.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Long> {
    Optional<Camera> findByIp(String ip);
    List<Camera> findByUserId(Long userId); // no findByRegisteredById
}
