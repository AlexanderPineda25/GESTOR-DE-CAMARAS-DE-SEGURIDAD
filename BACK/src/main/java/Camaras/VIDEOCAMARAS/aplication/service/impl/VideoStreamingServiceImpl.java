package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.CameraService;
import Camaras.VIDEOCAMARAS.aplication.service.RedisService;
import Camaras.VIDEOCAMARAS.aplication.service.VideoService;
import Camaras.VIDEOCAMARAS.aplication.service.VideoStreamingService;
import Camaras.VIDEOCAMARAS.aplication.service.impl.FrameGrabberService;
import Camaras.VIDEOCAMARAS.domain.model.enums.VideoStatus;
import Camaras.VIDEOCAMARAS.infraestructure._Websocket.WebSocketSessionAdapter;
import Camaras.VIDEOCAMARAS.infraestructure.observer.VideoStreamingObserver;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.bytedeco.ffmpeg.global.avutil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class VideoStreamingServiceImpl implements VideoStreamingService {

    private static final Logger log = LoggerFactory.getLogger(VideoStreamingServiceImpl.class);

    private final FrameGrabberService frameGrabberService;
    private final Map<String, WebSocketSessionAdapter> activeSessions = new ConcurrentHashMap<>();
    private final ExecutorService streamingExecutor;
    private final Java2DFrameConverter converter = new Java2DFrameConverter();
    private final List<VideoStreamingObserver> observers;
    private final VideoService videoService;
    private final CameraService cameraService;
    private final Map<String, Long> sessionPropertiesMap = new ConcurrentHashMap<>();
    private final RedisService redisService;

    @Value("${streaming.max.frame.size:1048576}")
    private int maxFrameSize;

    @Value("${streaming.target.fps:25}")
    private int targetFps;

    @Value("${streaming.frame.timeout.ms:5000}")
    private long frameTimeoutMs;

    public VideoStreamingServiceImpl(FrameGrabberService frameGrabberService,
                                     List<VideoStreamingObserver> observers,
                                     @Value("${streaming.thread.pool.size:10}") int threadPoolSize,
                                     VideoService videoService,
                                     CameraService cameraService,
                                     RedisService redisService) {
        this.frameGrabberService = frameGrabberService;
        this.observers = new CopyOnWriteArrayList<>(observers);
        this.streamingExecutor = Executors.newFixedThreadPool(threadPoolSize);
        this.videoService = videoService;
        this.cameraService = cameraService;
        this.redisService = redisService;
    }

    @Async
    @Override
    public void startStreaming(Long cameraId, WebSocketSessionAdapter session) {
        String sessionId = session.getId();
        log.info("Solicitando inicio de streaming para cámara {} y sesión {}", cameraId, sessionId);

        activeSessions.put(sessionId, session);

        try {
            notifyObserversOfStart(cameraId, sessionId);
        } catch (Exception e) {
            log.error("Error notificando inicio de streaming a observadores para cámara {} sesión {}: {}", cameraId, sessionId, e.getMessage(), e);
        }

        try {
            streamingExecutor.submit(() -> streamVideo(cameraId, session));
            log.info("Streaming iniciado (async) para cámara {} y sesión {}. Sesiones activas: {}", cameraId, sessionId, activeSessions.size());
        } catch (Exception e) {
            log.error("Error al iniciar tarea de streaming async para cámara {} sesión {}: {}", cameraId, sessionId, e.getMessage(), e);
        }
    }

    private void streamVideo(Long cameraId, WebSocketSessionAdapter session) {
        String sessionId = session.getId();
        log.info("Entrando a bucle de streaming para cámara {} y sesión {}", cameraId, sessionId);

        String videoFileName = String.format("video_%d_%d.mp4", cameraId, System.currentTimeMillis());
        String storageDir = "C:/cctv_videos";
        String filePath = storageDir + "/" + videoFileName;

        // Para 480p widescreen
        int width = 854;
        int height = 480;
        int fps = targetFps; // Mejor para fluidez y bajo lag, ajusta según tu fuente

        long frameIndex = 0;

        try (FFmpegFrameGrabber grabber = frameGrabberService.createGrabber(cameraId)) {
            // Opciones de baja latencia
            grabber.setOption("fflags", "nobuffer");
            grabber.setOption("flags", "low_delay");
            grabber.setOption("framedrop", "1");
            grabber.setOption("max_delay", "100000");
            grabber.setOption("buffer_size", "4096");
            grabber.start();

            long lastFrameTime = System.currentTimeMillis();
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Java2DFrameConverter java2DConverter = new Java2DFrameConverter();
            long lastLogTime = System.currentTimeMillis();
            int framesThisSecond = 0;

            while (session.isOpen()) {
                framesThisSecond++;
                if (System.currentTimeMillis() - lastLogTime >= 1000) {
                    log.info("FPS efectivos: " + framesThisSecond);
                    framesThisSecond = 0;
                    lastLogTime = System.currentTimeMillis();
                }
                long frameStartTime = System.currentTimeMillis();
                Frame frame = grabber.grabImage();
                if (frame != null) {
                    // Convierte frame a Mat (color)
                    Mat mat = converter.convert(frame);

                    // Redimensiona a 480p (854x480)
                    Mat resized = new Mat();
                    opencv_imgproc.resize(mat, resized, new Size(width, height));

                    // Serializa resized (color) a JPEG para WebSocket y Redis
                    Frame resizedFrame = converter.convert(resized); // Mat -> Frame
                    BufferedImage img = java2DConverter.getBufferedImage(resizedFrame); // Frame -> BufferedImage
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "jpg", baos);
                    byte[] jpegBytes = baos.toByteArray();

                    // Envía en vivo al cliente
                    sendFrame(session, jpegBytes);

                    // Cachea en Redis con TTL
                    redisService.cacheVideoFragment(cameraId, (int) frameIndex, jpegBytes, 10);

                    // Libera memoria
                    mat.release();
                    resized.release();
                    frameIndex++;
                    lastFrameTime = System.currentTimeMillis();
                }

                if (System.currentTimeMillis() - lastFrameTime > frameTimeoutMs) {
                    log.warn("Timeout de frame en cámara {}. Intentando reconectar...", cameraId);
                    notifyObserversOfError(cameraId, sessionId, new TimeoutException("Frame timeout"));
                    reconnectCamera(grabber, cameraId);
                    lastFrameTime = System.currentTimeMillis();
                }

                // Sleep breve opcional
                // Thread.sleep(3);

                long elapsed = System.currentTimeMillis() - frameStartTime;
                long frameDelay = 1000 / fps;
                if (elapsed < frameDelay) {
                    Thread.sleep(frameDelay - elapsed);
                }
            }

            // Al terminar el streaming, reconstruye el video a partir de frames en Redis
            if (frameIndex > 0) {
                String outputFilePath = filePath;
                videoService.reconstruirVideoDesdeRedis(cameraId, 0, (int) (frameIndex - 1), outputFilePath);

                // Guarda video en base de datos (lee de disco el archivo final reconstruido)
                VideoDto videoDto = VideoDto.builder()
                        .cameraId(cameraId)
                        .filePath(outputFilePath)
                        .createdAt(LocalDateTime.now())
                        .status(VideoStatus.PROCESSED)
                        .duration(null)
                        .build();
                byte[] videoBytes = Files.readAllBytes(Paths.get(outputFilePath));
                videoService.saveVideo(videoDto, cameraId, videoBytes);

                // Opcional: cachea metadatos del video final en Redis
                redisService.cacheVideo(cameraId, "latest", videoDto);

                log.info("Video grabado y registrado en BD para cámara {}: {}", cameraId, outputFilePath);
            }
        } catch (Exception e) {
            log.error("Error en streaming de cámara {} sesión {}: {}", cameraId, sessionId, e.getMessage(), e);
            notifyObserversOfError(cameraId, sessionId, e);
            handleStreamingError(session, e);
        } finally {
            notifyObserversOfStop(cameraId, sessionId);
            cleanupSession(sessionId);
            log.info("Streaming finalizado para cámara {} y sesión {}", cameraId, sessionId);
        }
    }

    private void streamFromRedis(Long cameraId, WebSocketSessionAdapter session, int nFrames, int fps) throws IOException, InterruptedException {
        String sessionId = session.getId();
        log.info("Iniciando streaming desde Redis para cámara {}, sesión {}, últimos {} frames a {} FPS", cameraId, sessionId, nFrames, fps);

        // Encuentra el último frame almacenado (puedes guardar un contador en Redis, o pasarlo por parámetro)
        int lastFrameIndex = redisService.getLastFrameIndexFromRedis(cameraId); // Implementa esto según tu lógica/código
        int firstFrameIndex = Math.max(0, lastFrameIndex - nFrames + 1);

        Java2DFrameConverter java2DConverter = new Java2DFrameConverter();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        for (int i = firstFrameIndex; i <= lastFrameIndex && session.isOpen(); i++) {
            Optional<byte[]> frameOpt = redisService.getVideoFragment(cameraId, i);
            if (frameOpt.isPresent()) {
                byte[] jpegBytes = frameOpt.get();

                // Decodifica el JPEG, asegurando que sea color
                ByteArrayInputStream bais = new ByteArrayInputStream(jpegBytes);
                BufferedImage img = ImageIO.read(bais);
                if (img == null) {
                    log.warn("Frame en Redis {} inválido o corrupto, se omite", i);
                    continue;
                }

                // Garantiza color (si llega a ser grayscale)
                Frame frame = java2DConverter.convert(img);
                Mat mat = converter.convert(frame);

                Mat bgrMat = new Mat();
                if (mat.channels() == 1) {
                    opencv_imgproc.cvtColor(mat, bgrMat, opencv_imgproc.COLOR_GRAY2BGR);
                } else {
                    bgrMat = mat;
                }

                // Si quieres redimensionar otra vez:
                // Mat resized = new Mat();
                // opencv_imgproc.resize(bgrMat, resized, new Size(854, 480));
                // bgrMat = resized;

                // Serializa a JPEG para enviar al cliente (color)
                Frame bgrFrame = converter.convert(bgrMat);
                BufferedImage colorImg = java2DConverter.getBufferedImage(bgrFrame);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(colorImg, "jpg", baos);
                byte[] outBytes = baos.toByteArray();

                sendFrame(session, outBytes);

                mat.release();
                if (mat.channels() == 1) bgrMat.release();
                // if usaste resized, resized.release();
            } else {
                log.warn("No se encontró frame {} en Redis para cámara {}", i, cameraId);
            }
            // Mantén el FPS deseado
            Thread.sleep(1000 / fps);
        }

        log.info("Finalizó streaming desde Redis para cámara {} sesión {}", cameraId, sessionId);
    }

    private byte[] convertFrameToBytes(Frame frame) throws IOException {
        BufferedImage image = converter.getBufferedImage(frame);
        if (image == null) {
            log.warn("No se pudo convertir frame a imagen (null)");
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            boolean written = ImageIO.write(image, "jpg", baos);
            if (!written) {
                log.warn("ImageIO.write retornó false, no se escribió la imagen JPEG");
                return null;
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error al convertir frame a bytes JPEG: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Override
    public synchronized void sendFrame(WebSocketSessionAdapter session, byte[] frame) throws IOException {
        String sessionId = session.getId();
        if (!session.isOpen()) {
            log.warn("Intento de enviar frame a sesión cerrada: {}", sessionId);
            return;
        }
        try {
            session.sendMessage(frame);
            log.debug("Frame enviado a sesión {}", sessionId);
        } catch (IOException e) {
            log.error("Error enviando frame a la sesión {}: {}", sessionId, e.getMessage(), e);
            cleanupSession(sessionId);
            throw e;
        }
    }


    @Override
    public void stopStreaming(WebSocketSessionAdapter session) throws IOException {
        String sessionId = session.getId();
        Long cameraId = getCameraIdFromSession(session);

        try {
            notifyObserversOfStop(cameraId, sessionId);
        } catch (Exception e) {
            log.error("Error notificando parada de streaming para cámara {} sesión {}: {}", cameraId, sessionId, e.getMessage(), e);
        }

        cleanupSession(sessionId);

        log.info("stopStreaming invocado para cámara {} y sesión {}", cameraId, sessionId);
    }


    @Override
    public byte[] captureFrame(Long cameraId) {
        try {
            Frame frame = frameGrabberService.grabLatestFrame(cameraId);
            if (frame == null) {
                String msg = "Frame nulo recibido en captureFrame para cámara " + cameraId;
                log.warn(msg);
                throw new RuntimeException(msg);
            }
            return convertFrameToBytes(frame);
        } catch (Exception e) {
            log.error("Error en captureFrame cámara {}: {}", cameraId, e.getMessage(), e);
            throw new RuntimeException("Error en captureFrame: " + e.getMessage(), e);
        }
    }


    private void notifyObserversOfStart(Long cameraId, String sessionId) {
        observers.forEach(observer -> {
            try {
                observer.onStreamingStarted(cameraId, sessionId);
            } catch (Exception e) {
                log.error("Error notificando inicio de streaming al observer {}: {}", observer.getClass().getSimpleName(), e.getMessage(), e);
            }
        });
    }

    private void notifyObserversOfFrameSent(Long cameraId, String sessionId, int frameSize) {
        observers.forEach(observer -> {
            try {
                observer.onFrameSent(cameraId, sessionId, frameSize);
            } catch (Exception e) {
                log.error("Error notificando frame enviado al observer {}: {}", observer.getClass().getSimpleName(), e.getMessage(), e);
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
        WebSocketSessionAdapter session = activeSessions.remove(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.close();
                log.info("Sesión {} cerrada correctamente", sessionId);
            } catch (IOException e) {
                log.error("Error cerrando sesión {}: {}", sessionId, e.getMessage(), e);
            }
        }
    }

    private void handleStreamingError(WebSocketSessionAdapter session, Exception e) {
        log.error("Error en streaming sesión {}: {}", session.getId(), e.getMessage(), e);
        cleanupSession(session.getId());
    }

    private Long getCameraIdFromSession(WebSocketSessionAdapter session) {
        Long cameraId = sessionPropertiesMap.get(session.getId()); // si tienes este mapa
        if (cameraId == null) {
            log.warn("No se encontró cameraId para la sesión {}", session.getId());
        }
        return cameraId;
    }

    public String processCommand(String command) {
        log.debug("Procesando comando: {}", command);
        // Lógica de procesamiento de comandos si aplica
        return "COMANDO_PROCESADO: " + command;
    }

    @Override
    public void processVideoFrame(byte[] frame, WebSocketSessionAdapter session) {
        String sessionId = session.getId();
        log.debug("Procesando frame recibido para sesión {}", sessionId);

        if (frame == null || frame.length == 0) {
            log.warn("Frame recibido nulo o vacío en sesión {}", sessionId);
            return;
        }

        log.info("Frame recibido con tamaño: {} bytes en sesión {}", frame.length, sessionId);
    }
}
