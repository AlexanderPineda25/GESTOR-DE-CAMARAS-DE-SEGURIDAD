package Camaras.VIDEOCAMARAS.infraestructure.controller;

import Camaras.VIDEOCAMARAS.aplication.service.VideoService;
import Camaras.VIDEOCAMARAS.domain.model.enums.VideoStatus;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    // Subir video para una cámara
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<VideoDto> uploadVideo(
            @RequestParam Long cameraId,
            @RequestParam MultipartFile videoFile) {

        try {
            byte[] videoBytes = videoFile.getBytes();

            VideoDto videoDto = VideoDto.builder()
                    .cameraId(cameraId)
                    .status(VideoStatus.IN_QUEUE)
                    // duration y filePath los pone el servicio luego
                    .build();

            VideoDto savedVideo = videoService.saveVideo(videoDto, cameraId, videoBytes);
            log.info("Video subido correctamente: id={}, cameraId={}", savedVideo.getId(), cameraId);
            return new ResponseEntity<>(savedVideo, HttpStatus.CREATED);
        } catch (IOException e) {
            log.error("Error de IO subiendo video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException | NotFoundException e) {
            log.warn("Error subiendo video para cámara {}: {}", cameraId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error inesperado subiendo video: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Listar videos por cámara
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<List<VideoDto>> getVideosByCamera(@PathVariable Long cameraId) {
        try {
            List<VideoDto> videos = videoService.findByCameraId(cameraId);
            log.info("Videos consultados para cámara {}: total={}", cameraId, videos.size());
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            log.error("Error consultando videos por cámara {}: {}", cameraId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener video por id (metadata)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{videoId}")
    public ResponseEntity<VideoDto> getVideoById(@PathVariable Long videoId) {
        try {
            Optional<VideoDto> videoOpt = videoService.findById(videoId);
            if (videoOpt.isPresent()) {
                log.info("Video encontrado: id={}", videoId);
                return ResponseEntity.ok(videoOpt.get());
            } else {
                log.warn("Video no encontrado: id={}", videoId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error buscando video por id={}: {}", videoId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener último video de cámara
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/camera/{cameraId}/latest")
    public ResponseEntity<VideoDto> getLatestVideoByCamera(@PathVariable Long cameraId) {
        try {
            Optional<VideoDto> videoOpt = videoService.findLatestByCameraId(cameraId);
            if (videoOpt.isPresent()) {
                log.info("Último video encontrado para cámara {}: id={}", cameraId, videoOpt.get().getId());
                return ResponseEntity.ok(videoOpt.get());
            } else {
                log.warn("No hay videos recientes para cámara {}", cameraId);
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("Error buscando último video de cámara {}: {}", cameraId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Descargar o reproducir video
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{videoId}/download")
    public ResponseEntity<Resource> downloadVideo(@PathVariable Long videoId) {
        try {
            Resource videoResource = videoService.getVideoResource(videoId);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + videoResource.getFilename() + "\"");

            log.info("Video descargado: id={}", videoId);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(videoResource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(videoResource);

        } catch (NotFoundException e) {
            log.warn("Archivo de video no encontrado: id={}", videoId);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error de IO descargando video id={}: {}", videoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error inesperado descargando video id={}: {}", videoId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    // Obtener video desde cache (por cameraId y clave de cache)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{cameraId}/cache")
    public ResponseEntity<VideoDto> getVideoFromCache(
            @PathVariable Long cameraId,
            @RequestParam String videoKey) {
        try {
            Optional<VideoDto> cachedVideo = videoService.getVideoFromCache(cameraId, videoKey);
            if (cachedVideo.isPresent()) {
                log.info("Video obtenido de cache para cameraId={}, key={}", cameraId, videoKey);
                return ResponseEntity.ok(cachedVideo.get());
            } else {
                log.warn("No se encontró video en cache para cameraId={}, key={}", cameraId, videoKey);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error obteniendo video de cache cameraId={}, key={}: {}", cameraId, videoKey, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
