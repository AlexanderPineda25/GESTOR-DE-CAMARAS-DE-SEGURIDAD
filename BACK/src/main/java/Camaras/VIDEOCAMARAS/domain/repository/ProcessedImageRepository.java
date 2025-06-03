package Camaras.VIDEOCAMARAS.domain.repository;

import Camaras.VIDEOCAMARAS.domain.model.ProcessedImage;
import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessedImageRepository extends JpaRepository<ProcessedImage, Long> {
    // Ejemplo de métodos útiles:
    List<ProcessedImage> findByOriginalImageId(Long originalImageId);
    List<ProcessedImage> findByFilterType(FilterType filterType);
    List<ProcessedImage> findByOriginalImage_Camera_Id(Long cameraId);
    List<ProcessedImage> findByOriginalImage_Camera_User_Id(Long userId);
}

