package Camaras.VIDEOCAMARAS.infraestructure.mapper;

import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.GeoLocation;
import Camaras.VIDEOCAMARAS.shared.dto.CameraDto;
import Camaras.VIDEOCAMARAS.shared.dto.CameraResponseDto;
import Camaras.VIDEOCAMARAS.shared.dto.GeoLocationDto;

public class CameraMapper {

    private CameraMapper() {}

    public static CameraResponseDto toResponseDto(Camera camera) {
        if (camera == null) return null;
        return CameraResponseDto.builder()
                .id(camera.getId())
                .ip(camera.getIp())
                .geoLocation(toGeoLocationDto(camera.getGeoLocation()))
                .status(camera.getStatus())
                .brand(camera.getBrand())
                .model(camera.getModel())
                .resolution(camera.getResolution())
                .streamUrl(camera.getStreamUrl())
                .user(camera.getUser() != null ? UserMapper.toDto(camera.getUser()) : null)
                .build();
    }

    public static CameraDto toDto(Camera camera) {
        if (camera == null) return null;
        return CameraDto.builder()
                .id(camera.getId())
                .ip(camera.getIp())
                .geoLocation(toGeoLocationDto(camera.getGeoLocation()))
                .status(camera.getStatus())
                .brand(camera.getBrand())
                .model(camera.getModel())
                .resolution(camera.getResolution())
                .streamUrl(camera.getStreamUrl())
                .user(camera.getUser() != null ? UserMapper.toDto(camera.getUser()) : null)
                .build();
    }

    // Helper
    private static GeoLocationDto toGeoLocationDto(GeoLocation geo) {
        if (geo == null) return null;
        return GeoLocationDto.builder()
                .latitude(geo.getLatitude())
                .longitude(geo.getLongitude())
                .build();
    }
}


