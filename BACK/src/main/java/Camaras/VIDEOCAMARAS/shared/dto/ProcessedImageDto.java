package Camaras.VIDEOCAMARAS.shared.dto;

import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedImageDto implements Serializable {
    private Long id;
    private Long originalImageId;       // ID de la imagen original
    private FilterType filterType;      // Enum del filtro aplicado
    private String filePath;            // Ruta/URL donde se guarda la imagen procesada
    private byte[] rawImage;            // Binario si lo necesitas transferir
    private LocalDateTime processedAt;  // Fecha/hora de procesamiento
}
