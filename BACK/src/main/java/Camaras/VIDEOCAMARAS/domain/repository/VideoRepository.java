package Camaras.VIDEOCAMARAS.domain.repository;

import Camaras.VIDEOCAMARAS.domain.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByCameraId(Long cameraId);
    Optional<Video> findTopByCameraIdOrderByCreatedAtDesc(Long cameraId);
}
