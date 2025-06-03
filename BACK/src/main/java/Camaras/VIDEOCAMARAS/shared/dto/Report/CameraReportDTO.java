package Camaras.VIDEOCAMARAS.shared.dto.Report;

import Camaras.VIDEOCAMARAS.domain.model.GeoLocation;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CameraReportDTO {
    private Long id;
    private String ip;
    private GeoLocation geoLocation;
    private String brand;
    private String model;
    private String resolution;
    private String streamUrl;
    private String status;
    private LocalDateTime registrationDate;
    private List<VideoReportDTO> videos;
    private List<ImageReportDTO> images;
}

