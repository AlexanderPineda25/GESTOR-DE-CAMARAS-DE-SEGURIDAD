package Camaras.VIDEOCAMARAS.infraestructure.controller;

import Camaras.VIDEOCAMARAS.aplication.service.ImageService;
import Camaras.VIDEOCAMARAS.shared.dto.ImageDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/{cameraId}/save")
    public ResponseEntity<ImageDto> saveImage(
            @PathVariable Long cameraId,
            @RequestBody ImageDto imageDto) {
        try {
            ImageDto saved = imageService.saveImage(imageDto, cameraId);
            log.info("Imagen guardada vía API: id={}, cameraId={}", saved.getId(), cameraId);
            return ResponseEntity.ok(saved);
        } catch (NotFoundException e) {
            log.warn("Cámara no encontrada para guardar imagen: cameraId={}", cameraId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al guardar imagen: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{cameraId}")
    public ResponseEntity<List<ImageDto>> getImagesByCameraId(@PathVariable Long cameraId) {
        try {
            List<ImageDto> images = imageService.findImagesByCameraId(cameraId);
            log.info("Consulta de imágenes para cameraId={}, total={}", cameraId, images.size());
            return ResponseEntity.ok(images);
        } catch (NotFoundException e) {
            log.warn("Cámara no encontrada al listar imágenes: cameraId={}", cameraId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error obteniendo imágenes de cámara {}: {}", cameraId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-id/{imageId}")
    public ResponseEntity<ImageDto> getImageById(@PathVariable Long imageId) {
        try {
            Optional<ImageDto> imageOpt = imageService.findById(imageId);
            if (imageOpt.isPresent()) {
                log.info("Imagen encontrada: id={}", imageId);
                return ResponseEntity.ok(imageOpt.get());
            } else {
                log.warn("No se encontró imagen: id={}", imageId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error consultando imagen por id={}: {}", imageId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            log.info("Imagen eliminada vía API: id={}", imageId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error eliminando imagen id={}: {}", imageId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{cameraId}/capture")
    public ResponseEntity<ImageDto> registerCapturedImage(
            @PathVariable Long cameraId,
            @RequestBody ImageDto imageDto) {
        try {
            ImageDto saved = imageService.registerCapturedImage(cameraId, imageDto);
            log.info("Imagen capturada y registrada vía API: id={}, cameraId={}", saved.getId(), cameraId);
            return ResponseEntity.ok(saved);
        } catch (NotFoundException e) {
            log.warn("Cámara no encontrada para registrar imagen capturada: cameraId={}", cameraId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error registrando imagen capturada: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
