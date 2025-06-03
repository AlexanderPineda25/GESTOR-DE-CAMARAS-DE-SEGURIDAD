package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.GeoLocation;
import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.shared.dto.CameraDto;
import Camaras.VIDEOCAMARAS.shared.dto.GeoLocationDto;

import java.time.LocalDateTime;

public class CameraFactory {

    private CameraFactory() {}

    // Helper para mapear GeoLocationDto a GeoLocation
    private static GeoLocation mapToGeoLocation(GeoLocationDto dto) {
        if (dto == null) return null;
        return GeoLocation.builder()
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }

    // Crear cámara desde DTO y User
    public static Camera createCamera(CameraDto dto, User user) {
        if (dto == null) throw new IllegalArgumentException("CameraDto no puede ser null");
        if (user == null) throw new IllegalArgumentException("User no puede ser null");
        return Camera.builder()
                .ip(dto.getIp())
                .geoLocation(mapToGeoLocation(dto.getGeoLocation()))
                .status(dto.getStatus())
                .brand(dto.getBrand())
                .model(dto.getModel())
                .resolution(dto.getResolution())
                .streamUrl(dto.getStreamUrl())
                .user(user)
                .registrationDate(LocalDateTime.now())
                .build();
    }

    // Actualizar una cámara existente usando DTO (NO cambia user ni registrationDate)
    public static Camera updateCamera(Camera original, CameraDto dto) {
        return original.toBuilder()
                .ip(dto.getIp())
                .geoLocation(mapToGeoLocation(dto.getGeoLocation()))
                .status(dto.getStatus())
                .brand(dto.getBrand())
                .model(dto.getModel())
                .resolution(dto.getResolution())
                .streamUrl(dto.getStreamUrl())
                .build();
    }
}
