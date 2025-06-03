package Camaras.VIDEOCAMARAS.domain.model;

import Camaras.VIDEOCAMARAS.domain.model.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @Column(nullable = false)
    private String filePath; // Opcional si quieres guardar la ruta también

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "data", nullable = false)
    private byte[] data; // El video binario en la BD

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private Double duration; // Segundos

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;
}
