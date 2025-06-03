package Camaras.VIDEOCAMARAS.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "camera_id", nullable = false)
    private final Camera camera;

    @Column(nullable = false)
    private final String filePath; // Ruta o URL del archivo

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private final byte[] data; // Imagen binaria (opcional, por si la quieres en DB)

    @Column(nullable = false)
    private final LocalDateTime createdAt;

    @OneToMany(mappedBy = "originalImage", fetch = FetchType.LAZY)
    private Set<ProcessedImage> processedImages;
}
