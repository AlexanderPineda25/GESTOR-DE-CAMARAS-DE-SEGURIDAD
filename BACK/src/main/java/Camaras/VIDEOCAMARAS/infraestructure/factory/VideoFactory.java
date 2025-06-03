package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.Video;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;

public class VideoFactory {
    private VideoFactory() {}

    public static Video create(VideoDto dto, Camera camera) {
        if (dto == null) throw new IllegalArgumentException("VideoDto no puede ser null");
        if (camera == null) throw new IllegalArgumentException("Camera no puede ser null");
        if (dto.getFilePath() == null) throw new IllegalArgumentException("El archivo de video es obligatorio");

        return Video.builder()
                .filePath(dto.getFilePath())
                .createdAt(dto.getCreatedAt())
                .duration(dto.getDuration())
                .status(dto.getStatus())
                .camera(camera)
                // .data(dto.getRawVideo()) // Agrega si tu DTO trae el BLOB
                .build();
    }
}
