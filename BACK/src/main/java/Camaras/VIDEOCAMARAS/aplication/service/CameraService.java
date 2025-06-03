package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus;
import Camaras.VIDEOCAMARAS.shared.dto.*;

import java.util.List;
import java.util.Optional;

public interface CameraService {

    Camera findEntityById(Long id);
    CameraResponseDto createCamera(CameraCreateDto cameraCreateDto, String email);
    List<CameraResponseDto> getAllCameras();
    List<CameraResponseDto> getCamerasByUser(String email);
    CameraResponseDto updateCamera(Long id, CameraDto cameraDto, String email);
    void deleteCamera(Long id, String email);
    Optional<CameraResponseDto> findById(Long id);
    List<CameraResponseDto> getAllCameraLocations();
    List<CameraDto> findCamerasByUserId(Long userId);
    Camera createCameraIfNotExists(
            String ip,
            String brand,
            String model,
            String resolution,
            CameraStatus status,
            String streamUrl,
            double latitude,
            double longitude,
            String assignedUserEmail,
            String adminEmail);
}
