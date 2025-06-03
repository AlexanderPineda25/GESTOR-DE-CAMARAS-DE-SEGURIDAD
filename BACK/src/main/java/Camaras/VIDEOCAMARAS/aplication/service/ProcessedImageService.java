package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import Camaras.VIDEOCAMARAS.shared.dto.ProcessedImageDto;

import java.util.List;
import java.util.Optional;

public interface ProcessedImageService {

    ProcessedImageDto saveProcessedImage(ProcessedImageDto processedImageDto, Long originalImageId);
    List<ProcessedImageDto> findByOriginalImageId(Long originalImageId);
    List<ProcessedImageDto> findByFilterType(FilterType filterType);
    Optional<ProcessedImageDto> findById(Long id);
    void deleteProcessedImage(Long processedImageId);
    List<ProcessedImageDto> getAllProcessedImages();
    ProcessedImageDto processAndSaveImage(Long originalImageId, FilterType filterType);
    byte[] getProcessedImageBytes(Long id);
}
