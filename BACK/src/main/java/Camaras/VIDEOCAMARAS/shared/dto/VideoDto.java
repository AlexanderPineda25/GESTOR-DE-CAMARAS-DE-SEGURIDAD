package Camaras.VIDEOCAMARAS.shared.dto;

import Camaras.VIDEOCAMARAS.domain.model.enums.VideoStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true) // <--- agrega esto
@AllArgsConstructor
@NoArgsConstructor
public class VideoDto implements Serializable {
    private Long id;
    private String filePath;               // Ruta o URL donde se puede acceder al video grabado
    private LocalDateTime createdAt;       // Fecha/hora de creación
    private Double duration;               // Duración en segundos
    private VideoStatus status;            // Estado del video (PROCESSED, IN_QUEUE, FAILED)
    private Long cameraId;                 // ID de la cámara asociada
    private String cameraName;             // (opcional) Nombre de la cámara, útil para el frontend
}
