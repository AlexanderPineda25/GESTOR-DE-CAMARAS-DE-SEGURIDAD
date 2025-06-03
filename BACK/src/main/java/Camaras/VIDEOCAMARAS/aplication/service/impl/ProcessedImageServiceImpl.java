package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.ProcessedImageService;
import Camaras.VIDEOCAMARAS.domain.model.Image;
import Camaras.VIDEOCAMARAS.domain.model.ProcessedImage;
import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import Camaras.VIDEOCAMARAS.domain.repository.ImageRepository;
import Camaras.VIDEOCAMARAS.domain.repository.ProcessedImageRepository;
import Camaras.VIDEOCAMARAS.infraestructure.factory.ProcessedImageFactory;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.ProcessedImageMapper;
import Camaras.VIDEOCAMARAS.shared.dto.ProcessedImageDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProcessedImageServiceImpl implements ProcessedImageService {

    private static final Logger log = LoggerFactory.getLogger(ProcessedImageServiceImpl.class);

    private final ProcessedImageRepository processedImageRepository;
    private final ImageRepository imageRepository;
    private final ImageProcessingEngine imageProcessingEngine;

    public ProcessedImageServiceImpl(
            ProcessedImageRepository processedImageRepository,
            ImageRepository imageRepository,
            ImageProcessingEngine imageProcessingEngine
    ) {
        this.processedImageRepository = processedImageRepository;
        this.imageRepository = imageRepository;
        this.imageProcessingEngine = imageProcessingEngine;
    }

    @Override
    @Transactional
    public ProcessedImageDto saveProcessedImage(ProcessedImageDto processedImageDto, Long originalImageId) {
        try {
            Image originalImage = imageRepository.findById(originalImageId)
                    .orElseThrow(() -> new NotFoundException("Original image not found"));
            FilterType filterType = processedImageDto.getFilterType();
            ProcessedImage processedImage = ProcessedImageFactory.create(processedImageDto, originalImage, filterType);
            processedImage = processedImageRepository.save(processedImage);
            log.info("Processed image saved: id={}, originalImageId={}", processedImage.getId(), originalImageId);
            return ProcessedImageMapper.toDto(processedImage);
        } catch (Exception e) {
            log.error("Error saving processed image for originalImageId={}: {}", originalImageId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ProcessedImageDto> findByOriginalImageId(Long originalImageId) {
        try {
            List<ProcessedImageDto> result = processedImageRepository.findByOriginalImageId(originalImageId)
                    .stream()
                    .map(ProcessedImageMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Found {} processed images for originalImageId={}", result.size(), originalImageId);
            return result;
        } catch (Exception e) {
            log.error("Error fetching processed images by originalImageId={}: {}", originalImageId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ProcessedImageDto> findByFilterType(FilterType filterType) {
        try {
            List<ProcessedImageDto> result = processedImageRepository.findByFilterType(filterType)
                    .stream()
                    .map(ProcessedImageMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Found {} processed images with filterType={}", result.size(), filterType);
            return result;
        } catch (Exception e) {
            log.error("Error fetching processed images by filterType={}: {}", filterType, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<ProcessedImageDto> findById(Long id) {
        try {
            Optional<ProcessedImageDto> result = processedImageRepository.findById(id)
                    .map(ProcessedImageMapper::toDto);
            if (result.isPresent()) {
                log.info("Processed image found: id={}", id);
            } else {
                log.warn("Processed image not found: id={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("Error fetching processed image by id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteProcessedImage(Long processedImageId) {
        try {
            processedImageRepository.deleteById(processedImageId);
            log.info("Processed image deleted: id={}", processedImageId);
        } catch (Exception e) {
            log.error("Error deleting processed image id={}: {}", processedImageId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ProcessedImageDto> getAllProcessedImages() {
        try {
            List<ProcessedImageDto> result = processedImageRepository.findAll().stream()
                    .map(ProcessedImageMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Fetched all processed images, total={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error fetching all processed images: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProcessedImageDto processAndSaveImage(Long originalImageId, FilterType filterType) {
        try {
            Image originalImage = imageRepository.findById(originalImageId)
                    .orElseThrow(() -> new NotFoundException("Original image not found"));

            // Procesa la imagen usando el engine
            String processedFilePath = imageProcessingEngine.applyFilters(originalImage.getFilePath(), List.of(filterType));
            byte[] processedData = Files.readAllBytes(Path.of(processedFilePath));

            ProcessedImageDto processedImageDto = ProcessedImageDto.builder()
                    .originalImageId(originalImageId)
                    .filterType(filterType)
                    .rawImage(processedData)
                    .filePath(processedFilePath)
                    .processedAt(java.time.LocalDateTime.now())
                    .build();

            ProcessedImageDto saved = saveProcessedImage(processedImageDto, originalImageId);
            log.info("Image processed and saved: originalImageId={}, filterType={}", originalImageId, filterType);
            return saved;
        } catch (ImageProcessingEngine.ImageProcessingException | IOException e) {
            log.error("Error processing and saving image originalImageId={}, filterType={}: {}", originalImageId, filterType, e.getMessage(), e);
            throw new RuntimeException("Failed to process and save image", e);
        } catch (Exception e) {
            log.error("Unexpected error processing and saving image originalImageId={}, filterType={}: {}", originalImageId, filterType, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public byte[] getProcessedImageBytes(Long id) {
        try {
            ProcessedImage processedImage = processedImageRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Processed image not found"));
            Path path = Path.of(processedImage.getFilePath());
            byte[] data = Files.readAllBytes(path);
            log.info("Fetched bytes for processed image: id={}, size={} bytes", id, data.length);
            return data;
        } catch (IOException e) {
            log.error("Error reading image file for processed image id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error reading image file", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching bytes for processed image id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
