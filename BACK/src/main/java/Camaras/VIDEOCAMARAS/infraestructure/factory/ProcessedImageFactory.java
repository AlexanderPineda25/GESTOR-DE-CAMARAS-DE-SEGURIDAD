package Camaras.VIDEOCAMARAS.infraestructure.factory;

import Camaras.VIDEOCAMARAS.domain.model.Image;
import Camaras.VIDEOCAMARAS.domain.model.ProcessedImage;
import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import Camaras.VIDEOCAMARAS.shared.dto.ProcessedImageDto;

import java.time.LocalDateTime;

public class ProcessedImageFactory {

    private ProcessedImageFactory() {}

    public static ProcessedImage create(ProcessedImageDto dto, Image originalImage, FilterType filterType) {
        if (dto == null) throw new IllegalArgumentException("ProcessedImageDto no puede ser null");
        if (originalImage == null) throw new IllegalArgumentException("Image original no puede ser null");
        if (filterType == null) throw new IllegalArgumentException("FilterType no puede ser null");

        String filePath = dto.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("El filePath de la imagen procesada no puede ser null ni vacío");
        }

        return ProcessedImage.builder()
                .originalImage(originalImage)
                .filterType(filterType)
                .filePath(filePath)
                .data(dto.getRawImage()) // solo si lo manejas
                .processedAt(dto.getProcessedAt() != null ? dto.getProcessedAt() : LocalDateTime.now())
                .build();
    }
}
