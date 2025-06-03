package Camaras.VIDEOCAMARAS.infraestructure.mapper;

import Camaras.VIDEOCAMARAS.domain.model.ProcessedImage;
import Camaras.VIDEOCAMARAS.shared.dto.ProcessedImageDto;
import Camaras.VIDEOCAMARAS.shared.dto.Report.ProcessedImageReportDTO;

public class ProcessedImageMapper {

    private ProcessedImageMapper() {}

    public static ProcessedImageDto toDto(ProcessedImage processedImage) {
        if (processedImage == null) return null;

        return ProcessedImageDto.builder()
                .id(processedImage.getId())
                .originalImageId(
                        processedImage.getOriginalImage() != null
                                ? processedImage.getOriginalImage().getId()
                                : null
                )
                .filterType(processedImage.getFilterType())
                .filePath(processedImage.getFilePath())
                .rawImage(processedImage.getData()) // si tienes este campo en la entidad
                .processedAt(processedImage.getProcessedAt())
                .build();
    }

    public static ProcessedImageReportDTO toReportDto(ProcessedImage processedImage) {
        if (processedImage == null) return null;
        return ProcessedImageReportDTO.builder()
                .id(processedImage.getId())
                .filePath(processedImage.getFilePath())
                .processedAt(processedImage.getProcessedAt())
                .filterType(processedImage.getFilterType() != null ? processedImage.getFilterType().name() : null)
                .build();
    }

}
