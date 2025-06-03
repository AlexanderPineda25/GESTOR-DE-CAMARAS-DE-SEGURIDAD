package Camaras.VIDEOCAMARAS.shared.dto;

import Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CameraDto {
    private Long id;
    private String ip;
    private GeoLocationDto geoLocation;
    private String brand;
    private String model;
    private String resolution;
    private String streamUrl;
    private CameraStatus status;
    private UserDto user; // Cambiado de 'registeredBy' a 'user' para coincidir con la entidad
}
