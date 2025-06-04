package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.Image;
import Camaras.VIDEOCAMARAS.shared.dto.ImageDto;
import java.time.LocalDateTime;

public class ImageFactory {

    private ImageFactory() {}

    public static Image create(ImageDto dto, Camera camera) {
        if (dto == null) throw new IllegalArgumentException("ImageDto no puede ser null");
        if (camera == null) throw new IllegalArgumentException("Camera no puede ser null");

        return Image.builder()
                .filePath(dto.getFilePath())
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                .camera(camera)
                .data(dto.getRawImage())
                .build();
    }
    public static Image update(Image original, ImageDto dto) {
        return original.toBuilder()
                .filePath(dto.getFilePath())
                .data(dto.getRawImage()) // Solo si aplica
                .build();
    }
}
