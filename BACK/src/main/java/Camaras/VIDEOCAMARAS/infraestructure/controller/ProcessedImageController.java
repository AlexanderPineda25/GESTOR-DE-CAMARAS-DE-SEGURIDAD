package Camaras.VIDEOCAMARAS.infraestructure.controller;

import Camaras.VIDEOCAMARAS.aplication.service.ProcessedImageService;
import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import Camaras.VIDEOCAMARAS.shared.dto.ProcessedImageDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/processed-images")
public class ProcessedImageController {

    private static final Logger log = LoggerFactory.getLogger(ProcessedImageController.class);

    private final ProcessedImageService processedImageService;

    public ProcessedImageController(ProcessedImageService processedImageService) {
        this.processedImageService = processedImageService;
    }

    @PostMapping("/process")
    public ResponseEntity<ProcessedImageDto> processImage(
            @RequestParam Long originalImageId,
            @RequestParam String filterType) {
        try {
            FilterType filter = FilterType.valueOf(filterType);
            ProcessedImageDto result = processedImageService.processAndSaveImage(originalImageId, filter);
            log.info("Imagen procesada: originalImageId={}, filter={}", originalImageId, filter);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de filtro inválido: '{}'", filterType);
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            log.warn("No se encontró imagen original id={}: {}", originalImageId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error inesperado procesando imagen id={}: {}", originalImageId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProcessedImageDto>> getAllProcessedImages() {
        try {
            List<ProcessedImageDto> images = processedImageService.getAllProcessedImages();
            log.info("Consulta de imágenes procesadas: total={}", images.size());
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error obteniendo imágenes procesadas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessedImageDto> getProcessedImageById(@PathVariable Long id) {
        try {
            return processedImageService.findById(id)
                    .map(img -> {
                        log.info("Imagen procesada encontrada: id={}", id);
                        return ResponseEntity.ok(img);
                    })
                    .orElseGet(() -> {
                        log.warn("Imagen procesada no encontrada: id={}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error buscando imagen procesada id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadProcessedImage(@PathVariable Long id) {
        try {
            byte[] imageBytes = processedImageService.getProcessedImageBytes(id);
            log.info("Descarga de bytes de imagen procesada: id={}, size={} bytes", id, imageBytes.length);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Cambia si usas PNG u otro formato
                    .body(imageBytes);
        } catch (NotFoundException e) {
            log.warn("Imagen procesada para descarga no encontrada: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error descargando bytes de imagen procesada id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcessedImage(@PathVariable Long id) {
        try {
            processedImageService.deleteProcessedImage(id);
            log.info("Imagen procesada eliminada: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            log.warn("Intento de eliminar imagen procesada no encontrada: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error eliminando imagen procesada id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/filter/{filterType}")
    public ResponseEntity<List<ProcessedImageDto>> getProcessedImagesByFilter(@PathVariable String filterType) {
        try {
            FilterType filter = FilterType.valueOf(filterType);
            List<ProcessedImageDto> images = processedImageService.findByFilterType(filter);
            log.info("Consulta de imágenes procesadas por filtro {}: total={}", filter, images.size());
            return ResponseEntity.ok(images);
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de filtro inválido en consulta: '{}'", filterType);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error consultando imágenes procesadas por filtro {}: {}", filterType, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/original/{originalImageId}")
    public ResponseEntity<List<ProcessedImageDto>> getProcessedImagesByOriginal(@PathVariable Long originalImageId) {
        try {
            List<ProcessedImageDto> images = processedImageService.findByOriginalImageId(originalImageId);
            log.info("Consulta de imágenes procesadas por originalImageId {}: total={}", originalImageId, images.size());
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error consultando imágenes procesadas por originalImageId {}: {}", originalImageId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
