package Camaras.VIDEOCAMARAS.shared.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageUploadDto {
    private MultipartFile image;      // El archivo de imagen que sube el cliente
    private Long cameraId;            // La cámara de la que proviene la imagen
    private String description;       // (opcional) Algún comentario o descripción
}
