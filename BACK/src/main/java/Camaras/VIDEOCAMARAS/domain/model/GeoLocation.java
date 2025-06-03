package Camaras.VIDEOCAMARAS.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeoLocation {
    private Double latitude;
    private Double longitude;
}