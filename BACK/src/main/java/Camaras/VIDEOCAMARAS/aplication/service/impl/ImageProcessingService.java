package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.Image;
import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import Camaras.VIDEOCAMARAS.infraestructure.observer.ImageProcessingObserver;
import Camaras.VIDEOCAMARAS.shared.dto.ImageDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ImageProcessingService {

    private final List<ImageProcessingObserver> observers = new CopyOnWriteArrayList<>();
    private final ImageProcessingEngine imageProcessingEngine;

    public ImageProcessingService(ImageProcessingEngine imageProcessingEngine) {
        this.imageProcessingEngine = imageProcessingEngine;
    }

    public void attach(ImageProcessingObserver observer) {
        Objects.requireNonNull(observer, "Observer cannot be null");
        observers.add(observer);
    }

    public void detach(ImageProcessingObserver observer) {
        observers.remove(observer);
    }

    public Image applyFilters(ImageDto imageDto, List<FilterType> filters, Camera camera)
            throws ImageProcessingException {
        validateInput(imageDto, filters, camera);

        try {
            String processedFilePath = imageProcessingEngine.applyFilters(
                    imageDto.getFilePath(),
                    filters
            );

            Image processedImage = buildProcessedImage(processedFilePath, camera);

            notifyObservers(processedImage);

            return processedImage;

        } catch (ImageProcessingEngine.ImageProcessingException e) {
            throw new ImageProcessingException("Error processing image", e);
        }
    }

    private void validateInput(ImageDto imageDto, List<FilterType> filters, Camera camera) {
        Objects.requireNonNull(imageDto, "ImageDto cannot be null");
        Objects.requireNonNull(filters, "Filter list cannot be null");
        Objects.requireNonNull(camera, "Camera cannot be null");

        if (imageDto.getFilePath() == null || imageDto.getFilePath().isBlank()) {
            throw new IllegalArgumentException("Invalid image path");
        }
    }

    private Image buildProcessedImage(String filePath, Camera camera) {
        return Image.builder()
                .filePath(filePath)
                .createdAt(LocalDateTime.now())
                .camera(camera)
                .build();
    }

    private void notifyObservers(Image image) {
        for (ImageProcessingObserver observer : observers) {
            try {
                observer.onImageProcessed(image);
            } catch (Exception e) {
                System.err.println("Error notifying observer: " + e.getMessage());
            }
        }
    }

    public static class ImageProcessingException extends Exception {
        public ImageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
