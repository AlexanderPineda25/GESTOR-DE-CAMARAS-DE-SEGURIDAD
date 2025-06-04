package Camaras.VIDEOCAMARAS.infraestructure.config;

import Camaras.VIDEOCAMARAS.aplication.service.CameraService;
import Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct; // Make sure this import is correct for your Spring Boot version
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CameraInitializer {

    private static final Logger logger = LoggerFactory.getLogger(CameraInitializer.class);

    private final CameraService cameraService;

    @Autowired
    public CameraInitializer(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    @PostConstruct
    public void initCameras() {
        try {
            cameraService.createCameraIfNotExists(
                    "192.168.1.6",
                    "Hikvision",
                    "DS-2CD2143G0-I",
                    "4MP",
                    CameraStatus.ONLINE,
                    "http://192.168.1.6:8080/video",
                    4.624335,
                    -74.063644,
                    "user@example.com",
                    "admin@example.com"
            );
            logger.info("Cámara con IP 192.168.1.6 inicializada/creada exitosamente.");
            cameraService.createCameraIfNotExists(
                    "192.168.1.3:8080",
                    "Dahua",
                    "IPC-HFW2231S-S-S2",
                    "2MP",
                    CameraStatus.OFFLINE,
                    "http://192.168.1.3:8080/video",
                    4.625800,
                    -74.065900,
                    "user@example.com",
                    "admin@example.com"
            );
            logger.info("Cámara con IP 192.168.1.7 inicializada/creada exitosamente.");
        } catch (Exception e) {
        }
    }
}