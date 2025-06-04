package Camaras.VIDEOCAMARAS.domain.model;

import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Table(name = "processed_image")
public class ProcessedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "original_image_id", nullable = false)
    private final Image originalImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final FilterType filterType;

    @Column(nullable = false)
    private final String filePath;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private final byte[] data;

    @Column(nullable = false)
    private final LocalDateTime processedAt;
}
