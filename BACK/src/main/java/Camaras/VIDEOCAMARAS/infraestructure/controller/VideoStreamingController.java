package Camaras.VIDEOCAMARAS.infraestructure.controller;

import Camaras.VIDEOCAMARAS.aplication.service.VideoStreamingManager;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/streaming")
public class VideoStreamingController {

    private static final Logger log = LoggerFactory.getLogger(VideoStreamingController.class);
    private final VideoStreamingManager videoStreamingManager;

    public VideoStreamingController(VideoStreamingManager videoStreamingManager) {
        this.videoStreamingManager = videoStreamingManager;
    }

    @GetMapping("/http/{cameraId}")
    public ResponseEntity<StreamingResponseBody> streamVideoHttp(@PathVariable Long cameraId) {
        log.info("Solicitando HTTP streaming para cámara {}", cameraId);
        try {
            StreamingResponseBody stream = videoStreamingManager.streamVideoHttp(cameraId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(stream);
        } catch (Exception e) {
            log.error("Error en HTTP streaming para cámara {}: {}", cameraId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Descargar frame/snapshot en JPEG (ejemplo: /api/streaming/snapshot/5)
     */
    @GetMapping("/snapshot/{cameraId}")
    public ResponseEntity<byte[]> captureFrame(@PathVariable Long cameraId) {
        log.info("Solicitando snapshot para cámara {}", cameraId);
        try {
            byte[] frame = videoStreamingManager.captureFrame(cameraId);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(frame);
        } catch (Exception e) {
            log.error("Error obteniendo snapshot para cámara {}: {}", cameraId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * HTTP streaming (alias para compatibilidad, opcional)
     */
    @GetMapping("/video/{cameraId}")
    public ResponseEntity<StreamingResponseBody> streamVideo(@PathVariable Long cameraId) {
        log.info("Solicitando streamVideo para cámara {}", cameraId);
        try {
            StreamingResponseBody stream = videoStreamingManager.streamVideo(cameraId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(stream);
        } catch (Exception e) {
            log.error("Error en streamVideo para cámara {}: {}", cameraId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
