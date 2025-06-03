package Camaras.VIDEOCAMARAS.shared.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto implements Serializable {
    private Long id;
    private String filePath;              // Mejor que "filePath", o usa imageUrl si aplica
    private LocalDateTime createdAt;
    private Long cameraId;
    private String cameraName;
    private byte[] rawImage;                 // Para procesamiento (opcional)
}

