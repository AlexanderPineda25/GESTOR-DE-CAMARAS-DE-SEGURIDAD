package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.CameraService;
import Camaras.VIDEOCAMARAS.aplication.service.VideoService;
import Camaras.VIDEOCAMARAS.aplication.service.VideoStreamingService;
import Camaras.VIDEOCAMARAS.aplication.service.impl.FrameGrabberService;
import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.enums.VideoStatus;
import Camaras.VIDEOCAMARAS.infraestructure.observer.VideoStreamingObserver;
import Camaras.VIDEOCAMARAS.shared.dto.CameraResponseDto;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.Session;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static graphql.introspection.IntrospectionQueryBuilder.build;

@Service
public class VideoStreamingServiceImpl implements VideoStreamingService {

    private static final Logger log = LoggerFactory.getLogger(VideoStreamingServiceImpl.class);

    private final FrameGrabberService frameGrabberService;
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    private final ExecutorService streamingExecutor;
    private final Java2DFrameConverter converter = new Java2DFrameConverter();
    private final List<VideoStreamingObserver> observers;
    private final VideoService videoService; // Asegúrate de inyectar tu servicio de video
    private final CameraService cameraService;

    @Value("${streaming.max.frame.size:1048576}")
    private int maxFrameSize;

    @Value("${streaming.target.fps:25}")
    private int targetFps;

    @Value("${streaming.frame.timeout.ms:5000}")
    private long frameTimeoutMs;

    public VideoStreamingServiceImpl(
            FrameGrabberService frameGrabberService,
            List<VideoStreamingObserver> observers,
            @Value("${streaming.thread.pool.size:10}") int threadPoolSize,
            VideoService videoService,
            CameraService cameraService) {
        this.frameGrabberService = frameGrabberService;
        this.observers = new CopyOnWriteArrayList<>(observers);
        this.streamingExecutor = Executors.newFixedThreadPool(threadPoolSize);
        this.videoService = videoService;
        this.cameraService = cameraService;
    }

    @Override
    @Async
    public void startStreaming(Long cameraId, Session session) throws IOException {
        String sessionId = session.getId();
        log.info("Solicitando inicio de streaming para cámara {} y sesión {}", cameraId, sessionId);

        // NO verifiques permisos aquí, debe hacerlo el controller/capa de seguridad
        activeSessions.put(sessionId, session);
        notifyObserversOfStart(cameraId, sessionId);
        streamingExecutor.submit(() -> streamVideo(cameraId, session));
        log.info("Streaming iniciado (async) para cámara {} y sesión {}", cameraId, sessionId);
    }

    private void streamVideo(Long cameraId, Session session) {
        String sessionId = session.getId();
        log.info("Entrando a bucle de streaming para cámara {} y sesión {}", cameraId, sessionId);

        String videoFileName = String.format("video_%d_%d.mp4", cameraId, System.currentTimeMillis());
        String storageDir = "/C:/cctv_videos"; //
        String filePath = storageDir + "/" + videoFileName;

        // Opcional: Ajusta width/height según tu cámara o config
        int width = 1280;
        int height = 720;

        FFmpegFrameRecorder recorder = null;

        try (FFmpegFrameGrabber grabber = frameGrabberService.createGrabber(cameraId)) {
            grabber.start();

            recorder = new FFmpegFrameRecorder(filePath, width, height);
            recorder.setFormat("mp4");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // Importa org.bytedeco.ffmpeg.global.avcodec
            recorder.setFrameRate(targetFps);
            recorder.start();

            long frameIntervalMs = 1000 / targetFps;
            long lastFrameTime = System.currentTimeMillis();

            while (session.isOpen()) {
                Frame frame = grabber.grabImage();
                if (frame != null) {
                    // 1. Graba el frame en el archivo de video
                    recorder.record(frame);

                    // 2. Envía el frame al cliente (WebSocket)
                    byte[] frameBytes = convertFrameToBytes(frame);
                    if (frameBytes != null && frameBytes.length <= maxFrameSize) {
                        sendFrame(session, frameBytes);
                        notifyObserversOfFrameSent(cameraId, sessionId, frameBytes.length);
                        lastFrameTime = System.currentTimeMillis();
                    } else {
                        log.warn("Frame demasiado grande o nulo en cámara {} (size={})", cameraId, (frameBytes != null ? frameBytes.length : 0));
                    }
                }
                if (System.currentTimeMillis() - lastFrameTime > frameTimeoutMs) {
                    log.warn("Timeout de frame en cámara {}. Intentando reconectar...", cameraId);
                    notifyObserversOfError(cameraId, sessionId, new TimeoutException("Frame timeout"));
                    reconnectCamera(grabber, cameraId);
                    lastFrameTime = System.currentTimeMillis();
                }
                TimeUnit.MILLISECONDS.sleep(frameIntervalMs);
            }
        } catch (Exception e) {
            log.error("Error en streaming de cámara {} sesión {}: {}", cameraId, sessionId, e.getMessage(), e);
            notifyObserversOfError(cameraId, sessionId, e);
            handleStreamingError(session, e);
        } finally {
            try {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
            } catch (Exception ex) {
                log.error("Error cerrando recorder de cámara {}: {}", cameraId, ex.getMessage(), ex);
            }

            // --- Registro en base de datos del video grabado ---
            try {
                // (Inyecta VideoService en tu clase si no lo tienes)
                VideoDto videoDto = VideoDto.builder()
                        .cameraId(cameraId)
                        .filePath(filePath)
                        .createdAt(java.time.LocalDateTime.now())
                        .status(VideoStatus.PROCESSED) // O el que corresponda
                        .duration(null) // Puedes calcular la duración si quieres
                        .build();
                videoService.saveVideo(videoDto, cameraId, java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
                log.info("Video grabado y registrado en BD para cámara {}: {}", cameraId, filePath);
            } catch (Exception ex) {
                log.error("Error registrando video en la base de datos tras grabar: {}", ex.getMessage(), ex);
            }

            notifyObserversOfStop(cameraId, sessionId);
            cleanupSession(sessionId);
            log.info("Streaming finalizado para cámara {} y sesión {}", cameraId, sessionId);
        }
    }


    private byte[] convertFrameToBytes(Frame frame) throws IOException {
        BufferedImage image = converter.getBufferedImage(frame);
        if (image == null) {
            log.warn("No se pudo convertir frame a imagen");
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        }
    }

    @Override
    public synchronized void sendFrame(Session session, byte[] frame) throws IOException {
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(frame));
                log.debug("Frame enviado a sesión {}", session.getId());
            } catch (IOException e) {
                log.error("Error enviando frame a la sesión {}: {}", session.getId(), e.getMessage(), e);
                cleanupSession(session.getId());
                throw e;
            }
        } else {
            log.warn("Intento de enviar frame a sesión cerrada: {}", session.getId());
        }
    }

    @Override
    public void stopStreaming(Session session) throws IOException {
        String sessionId = session.getId();
        Long cameraId = getCameraIdFromSession(session);
        notifyObserversOfStop(cameraId, sessionId);
        cleanupSession(sessionId);
        log.info("stopStreaming invocado para cámara {} y sesión {}", cameraId, sessionId);
    }

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

    // --- Métodos utilitarios internos para observer y gestión de sesión ---

    private void notifyObserversOfStart(Long cameraId, String sessionId) {
        observers.forEach(observer -> {
            try {
                observer.onStreamingStarted(cameraId, sessionId);
            } catch (Exception e) {
                log.error("Error notificando inicio de streaming al observer: {}", e.getMessage(), e);
            }
        });
    }

    private void notifyObserversOfFrameSent(Long cameraId, String sessionId, int frameSize) {
        observers.forEach(observer -> {
            try {
                observer.onFrameSent(cameraId, sessionId, frameSize);
            } catch (Exception e) {
                log.error("Error notificando frame enviado al observer: {}", e.getMessage(), e);
            }
        });
    }

    private void notifyObserversOfStop(Long cameraId, String sessionId) {
        observers.forEach(observer -> {
            try {
                observer.onStreamingStopped(cameraId, sessionId);
            } catch (Exception e) {
                log.error("Error notificando parada de streaming al observer: {}", e.getMessage(), e);
            }
        });
    }

    private void notifyObserversOfError(Long cameraId, String sessionId, Exception ex) {
        observers.forEach(observer -> {
            try {
                observer.onStreamingError(cameraId, sessionId, ex);
            } catch (Exception e) {
                log.error("Error notificando error de streaming al observer: {}", e.getMessage(), e);
            }
        });
    }

    private void reconnectCamera(FFmpegFrameGrabber grabber, Long cameraId) {
        try {
            grabber.restart();
            log.info("Reconexión exitosa para cámara {}", cameraId);
        } catch (Exception e) {
            log.error("Error al reconectar cámara {}: {}", cameraId, e.getMessage(), e);
        }
    }

    private void cleanupSession(String sessionId) {
        Session session = activeSessions.remove(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.close();
                log.info("Sesión {} cerrada correctamente", sessionId);
            } catch (IOException e) {
                log.error("Error cerrando sesión {}: {}", sessionId, e.getMessage(), e);
            }
        }
    }

    private void handleStreamingError(Session session, Exception e) {
        log.error("Error en streaming: {}", e.getMessage(), e);
        cleanupSession(session.getId());
    }

    private Long getCameraIdFromSession(Session session) {
        Object cameraId = session.getUserProperties().get("cameraId");
        if (cameraId == null) {
            log.warn("No se encontró cameraId en la sesión {}", session.getId());
            return null;
        }
        return (Long) cameraId;
    }

    public String processCommand(String command) {
        log.debug("Procesando comando: {}", command);
        // Lógica de procesamiento de comandos
        return "COMANDO_PROCESADO: " + command;
    }

}
