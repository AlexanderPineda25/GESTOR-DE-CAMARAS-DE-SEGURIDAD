package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.VideoStreamingManager;
import Camaras.VIDEOCAMARAS.aplication.service.VideoService;
import Camaras.VIDEOCAMARAS.domain.repository.CameraRepository;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class VideoStreamingManagerImpl implements VideoStreamingManager {

    private static final Logger log = LoggerFactory.getLogger(VideoStreamingManagerImpl.class);

    private final VideoService videoService;
    private final FrameGrabberService frameGrabberService;
    private final CameraRepository cameraRepository;

    @Value("${streaming.max.frame.size:1048576}") // 1MB por defecto
    private int maxFrameSize;

    @Value("${streaming.target.fps:25}")
    private int targetFps;

    @Value("${streaming.frame.timeout.ms:5000}")
    private long frameTimeoutMs;

    private final ExecutorService streamingExecutor = Executors.newFixedThreadPool(4);
    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    public VideoStreamingManagerImpl(VideoService videoService,
                                     FrameGrabberService frameGrabberService,
                                     CameraRepository cameraRepository) {
        this.videoService = videoService;
        this.frameGrabberService = frameGrabberService;
        this.cameraRepository = cameraRepository;
    }

    /**
     * Convierte un Frame de JavaCV a byte[] en formato JPEG.
     */
    private byte[] convertFrameToBytes(Frame frame) throws IOException {
        BufferedImage image = converter.getBufferedImage(frame);
        if (image == null) {
            log.warn("No se pudo convertir frame a imagen (null)");
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        }
    }

    /**
     * HTTP streaming de archivo MP4 grabado (no realtime).
     */
    @Override
    public StreamingResponseBody streamVideoHttp(Long cameraId) {
        return outputStream -> {
            try {
                Optional<VideoDto> videoOpt = videoService.findLatestByCameraId(cameraId);
                if (videoOpt.isEmpty() || videoOpt.get().getFilePath() == null) {
                    log.warn("No se encontró video para cámara {}", cameraId);
                    throw new NotFoundException("No se encontró video para la cámara");
                }
                String filePath = videoOpt.get().getFilePath();
                try (InputStream is = Files.newInputStream(Path.of(filePath))) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                        outputStream.flush();
                    }
                    log.info("Transmisión HTTP terminada para cámara {}", cameraId);
                }
            } catch (Exception e) {
                log.error("Error en streamVideoHttp cámara {}: {}", cameraId, e.getMessage(), e);
                throw new RuntimeException("Error al transmitir video: " + e.getMessage(), e);
            }
        };
    }

    /**
     * WebSocket streaming realtime (frame by frame, usando FrameGrabberService).
     */
    @Override
    public void startStreamingWebSocket(Long cameraId, Session session) {
        streamingExecutor.submit(() -> {
            String sessionId = session.getId();
            log.info("Iniciando WebSocket streaming para cámara {} y sesión {}", cameraId, sessionId);

            try (FFmpegFrameGrabber grabber = frameGrabberService.createGrabber(cameraId)) {
                grabber.start();
                long frameIntervalMs = 1000 / targetFps;
                long lastFrameTime = System.currentTimeMillis();

                while (session.isOpen()) {
                    Frame frame = grabber.grabImage();
                    if (frame != null) {
                        byte[] frameBytes = convertFrameToBytes(frame);
                        if (frameBytes != null && frameBytes.length <= maxFrameSize) {
                            session.getBasicRemote().sendBinary(ByteBuffer.wrap(frameBytes));
                            lastFrameTime = System.currentTimeMillis();
                            log.debug("Frame enviado a sesión {}", sessionId);
                        }
                    }
                    if (System.currentTimeMillis() - lastFrameTime > frameTimeoutMs) {
                        log.warn("Timeout en WebSocket streaming cámara {}", cameraId);
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(frameIntervalMs);
                }
            } catch (Exception e) {
                log.error("Error en streaming WebSocket cámara {}: {}", cameraId, e.getMessage(), e);
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Error de streaming"));
                } catch (IOException ioException) {
                    log.error("Error cerrando sesión WebSocket {}", sessionId, ioException);
                }
            }
            log.info("Streaming WebSocket terminado para cámara {}", cameraId);
        });
    }

    /**
     * Snapshot/captura de frame único usando FrameGrabberService.
     */
    @Override
    public byte[] captureFrame(Long cameraId) {
        try {
            Frame frame = frameGrabberService.grabLatestFrame(cameraId);
            if (frame == null) {
                log.warn("Frame nulo recibido en captureFrame para cámara {}", cameraId);
                throw new RuntimeException("Frame nulo recibido");
            }
            return convertFrameToBytes(frame);
        } catch (Exception e) {
            log.error("Error en captureFrame cámara {}: {}", cameraId, e.getMessage(), e);
            throw new RuntimeException("Error en captureFrame: " + e.getMessage(), e);
        }
    }

    /**
     * Alias para compatibilidad (puedes dirigir al HTTP o a un realtime si algún día quieres cambiar la lógica).
     */
    @Override
    public StreamingResponseBody streamVideo(Long cameraId) {
        return streamVideoHttp(cameraId); // Alias, mantiene compatibilidad
    }
}
