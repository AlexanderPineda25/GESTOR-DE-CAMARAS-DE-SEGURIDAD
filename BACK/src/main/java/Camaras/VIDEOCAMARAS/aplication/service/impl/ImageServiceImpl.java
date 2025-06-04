package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.ImageService;
import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.Image;
import Camaras.VIDEOCAMARAS.domain.repository.CameraRepository;
import Camaras.VIDEOCAMARAS.domain.repository.ImageRepository;
import Camaras.VIDEOCAMARAS.infraestructure.factory.ImageFactory;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.ImageMapper;
import Camaras.VIDEOCAMARAS.shared.dto.ImageDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.IOException;

@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final ImageRepository imageRepository;
    private final CameraRepository cameraRepository;

    public ImageServiceImpl(ImageRepository imageRepository, CameraRepository cameraRepository) {
        this.imageRepository = imageRepository;
        this.cameraRepository = cameraRepository;
    }

    @Override
    @Transactional
    public ImageDto saveImage(ImageDto imageDto, Long cameraId) {
        try {
            Camera camera = cameraRepository.findById(cameraId)
                    .orElseThrow(() -> new NotFoundException("Cámara no encontrada para guardar imagen"));

            String baseDir = "C:/cctv_images/";
            String fileName = "img_" + System.currentTimeMillis() + "_" + camera.getId() + ".png";
            String filePath = baseDir + fileName;

            Files.createDirectories(Path.of(baseDir));
            Files.write(Path.of(filePath), imageDto.getRawImage());

            ImageDto dtoWithPath = imageDto.toBuilder().filePath(filePath).build();
            Image image = ImageFactory.create(dtoWithPath, camera);
            image = imageRepository.save(image);

            log.info("Imagen guardada exitosamente: id={}, cameraId={}, filePath={}", image.getId(), cameraId, filePath);
            return ImageMapper.toDto(image);

        } catch (Exception e) {
            log.error("Error guardando imagen para cameraId={}: {}", cameraId, e.getMessage(), e);
            throw new RuntimeException("Error guardando imagen", e);
        }
    }



    // Obtiene todas las imágenes asociadas a una cámara
    @Override
    public List<ImageDto> findImagesByCameraId(Long cameraId) {
        try {
            Camera camera = cameraRepository.findById(cameraId)
                    .orElseThrow(() -> new NotFoundException("Cámara no encontrada para listar imágenes"));
            List<ImageDto> result = imageRepository.findByCamera(camera).stream()
                    .map(ImageMapper::toDto)
                    .collect(Collectors.toList());

            log.info("Obtenidas {} imágenes para cameraId={}", result.size(), cameraId);
            return result;
        } catch (Exception e) {
            log.error("Error obteniendo imágenes para cameraId={}: {}", cameraId, e.getMessage(), e);
            throw e;
        }
    }

    // Busca una imagen por su ID
    @Override
    public Optional<ImageDto> findById(Long id) {
        try {
            Optional<ImageDto> result = imageRepository.findById(id).map(ImageMapper::toDto);
            if (result.isPresent()) {
                log.info("Imagen encontrada: id={}", id);
            } else {
                log.warn("No se encontró imagen con id={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("Error buscando imagen por id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Elimina una imagen por su ID
    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        try {
            imageRepository.deleteById(imageId);
            log.info("Imagen eliminada exitosamente: id={}", imageId);
        } catch (Exception e) {
            log.error("Error eliminando imagen id={}: {}", imageId, e.getMessage(), e);
            throw e;
        }
    }

    // Registra una imagen capturada (wrapper para saveImage, con timestamp si es necesario)
    @Override
    @Transactional
    public ImageDto registerCapturedImage(Long cameraId, ImageDto imageDto) {
        try {
            Camera camera = cameraRepository.findById(cameraId)
                    .orElseThrow(() -> new NotFoundException("Cámara no encontrada para registrar imagen capturada"));

            ImageDto imageToSave = imageDto.toBuilder()
                    .cameraId(cameraId)
                    .createdAt(imageDto.getCreatedAt() != null ? imageDto.getCreatedAt() : LocalDateTime.now())
                    .build();

            Image image = ImageFactory.create(imageToSave, camera);
            image = imageRepository.save(image);

            log.info("Imagen capturada y guardada: id={}, cameraId={}", image.getId(), cameraId);
            return ImageMapper.toDto(image);
        } catch (Exception e) {
            log.error("Error registrando imagen capturada para cameraId={}: {}", cameraId, e.getMessage(), e);
            throw e;
        }
    }
}
