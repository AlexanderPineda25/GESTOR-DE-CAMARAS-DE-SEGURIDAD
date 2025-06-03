package Camaras.VIDEOCAMARAS.shared.dto;

import Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraCreateDto {
    private String ip;
    private GeoLocationDto geoLocation;
    private String brand;
    private String model;
    private String resolution;
    private String streamUrl;
    private Long userId; // Solo el ID para vincular usuario
    private CameraStatus status;
}

