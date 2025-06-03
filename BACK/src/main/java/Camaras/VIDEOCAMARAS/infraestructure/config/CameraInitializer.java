package Camaras.VIDEOCAMARAS.infraestructure.config;

import Camaras.VIDEOCAMARAS.aplication.service.CameraService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.DependsOn;

@Component
@DependsOn("userInitializer") // Usa el nombre del bean (por defecto, la clase en camelCase)
public class CameraInitializer {

    private final CameraService cameraService;

    public CameraInitializer(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    @PostConstruct
    public void initCameras() {
        try {
            cameraService.createCameraIfNotExists(
                    "192.168.1.4",
                    "Hikvision",
                    "DS-2CD2143G0-I",
                    "4MP",
                    Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus.ONLINE,
                    "http://192.168.1.4:8080/video",
                    4.624335,
                    -74.063644,
                    "user@example.com",
                    "admin@example.com"
            );
        } catch (IllegalArgumentException e) {
            System.out.println("Cámara 192.168.1.4 ya existe, no se crea de nuevo.");
        }

        try {
            cameraService.createCameraIfNotExists(
                    "192.168.1.11",
                    "Dahua",
                    "IPC-HDW2431T-AS",
                    "4MP",
                    Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus.ONLINE,
                    "http://www.skylinewebcams.com/es/webcam/united-states/tennessee/gatlinburg/tennessee-gatlinburg.html",
                    4.601710,
                    -74.066055,
                    "user@example.com",
                    "admin@example.com"
            );
        } catch (IllegalArgumentException e) {
            System.out.println("Cámara 192.168.1.11 ya existe, no se crea de nuevo.");
        }

        System.out.println("Inicialización de cámaras terminada.");
    }
}
