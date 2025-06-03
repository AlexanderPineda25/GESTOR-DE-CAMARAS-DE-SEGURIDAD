package Camaras.VIDEOCAMARAS.shared.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeoLocationDto {
    private Double latitude;
    private Double longitude;
}

