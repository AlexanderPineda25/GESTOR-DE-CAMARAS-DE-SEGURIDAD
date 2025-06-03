package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.shared.dto.ImageDto;

import java.util.List;
import java.util.Optional;

public interface ImageService {
    ImageDto saveImage(ImageDto imageDto, Long cameraId);
    List<ImageDto> findImagesByCameraId(Long cameraId);
    Optional<ImageDto> findById(Long id);
    void deleteImage(Long imageId); // Opcional
    ImageDto registerCapturedImage(Long cameraId, ImageDto imageDto);

}
