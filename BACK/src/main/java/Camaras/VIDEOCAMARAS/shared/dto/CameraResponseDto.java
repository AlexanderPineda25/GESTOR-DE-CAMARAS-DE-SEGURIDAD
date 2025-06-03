package Camaras.VIDEOCAMARAS.shared.dto;

import Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CameraResponseDto {
    private Long id;
    private String ip;
    private GeoLocationDto geoLocation;
    private CameraStatus status;
    private String brand;
    private String model;
    private String resolution;
    private String streamUrl;
    private UserDto user; // Cambiado de registeredBy a user
}
