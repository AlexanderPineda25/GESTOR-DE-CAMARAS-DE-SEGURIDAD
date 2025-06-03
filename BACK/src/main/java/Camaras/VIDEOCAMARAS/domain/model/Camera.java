package Camaras.VIDEOCAMARAS.domain.model;

import Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "camera")
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ip;

    @Embedded
    private GeoLocation geoLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CameraStatus status = CameraStatus.OFFLINE;

    private String brand;
    private String model;
    private String resolution;

    @Column(nullable = false)
    private String streamUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    public boolean isOnline() {
        return status == CameraStatus.ONLINE;
    }
}
