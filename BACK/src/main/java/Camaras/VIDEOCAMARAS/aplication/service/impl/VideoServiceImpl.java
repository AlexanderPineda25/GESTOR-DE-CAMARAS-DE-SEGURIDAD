package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.VideoService;
import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.Video;
import Camaras.VIDEOCAMARAS.domain.repository.CameraRepository;
import Camaras.VIDEOCAMARAS.domain.repository.VideoRepository;
import Camaras.VIDEOCAMARAS.infraestructure.factory.VideoFactory;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.VideoMapper;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import Camaras.VIDEOCAMARAS.aplication.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Value("${video.storage.path}")
    private String storagePath;

    @Value("${video.storage.max-size-mb:250}")
    private int maxSizeMb;

    @Value("${video.storage.auto-create-dirs:true}")
    private boolean autoCreateDirs;

    private final VideoRepository videoRepository;
    private final CameraRepository cameraRepository;
    private final RedisService redisService; // Inyecta si usas caché

    public VideoServiceImpl(VideoRepository videoRepository,
                            CameraRepository cameraRepository,
                            RedisService redisService) {
        this.videoRepository = videoRepository;
        this.cameraRepository = cameraRepository;
        this.redisService = redisService;
    }

    // Guarda un video en disco y lo registra en base de datos
    @Override
    @Transactional
    public VideoDto saveVideo(VideoDto videoDto, Long cameraId, byte[] videoData) {
        try {
            Camera camera = cameraRepository.findById(cameraId)
                    .orElseThrow(() -> new NotFoundException("Cámara no encontrada"));

            if (videoData == null || videoData.length == 0) {
                log.warn("No se recibieron datos de video para cámara id={}", cameraId);
                throw new IllegalArgumentException("No video data provided");
            }
            if (videoData.length > maxSizeMb * 1024 * 1024) {
                log.warn("El video excede el tamaño máximo permitido para cámara id={}", cameraId);
                throw new IllegalArgumentException("The video exceeds the maximum allowed size");
            }

            // Asegura directorio
            Path cameraPath = Paths.get(storagePath, camera.getId().toString());
            if (autoCreateDirs && !Files.exists(cameraPath)) {
                Files.createDirectories(cameraPath);
            }

            // Guarda a disco
            String fileName = "video_" + System.currentTimeMillis() + ".mp4";
            Path filePath = cameraPath.resolve(fileName);
            Files.write(filePath, videoData);

            // Registra en base de datos
            Video video = VideoFactory.create(videoDto, camera).toBuilder()
                    .filePath(filePath.toString())
                    .build();
            Video saved = videoRepository.save(video);

            log.info("Video guardado y registrado: id={}, cameraId={}", saved.getId(), cameraId);
            return VideoMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Error guardando video para cameraId={}: {}", cameraId, e.getMessage(), e);
            throw new RuntimeException("Error saving video: " + e.getMessage(), e);
        }
    }

    // Busca un video por ID
    @Override
    public Optional<VideoDto> findById(Long id) {
        try {
            Optional<VideoDto> result = videoRepository.findById(id).map(VideoMapper::toDto);
            if (result.isPresent()) {
                log.info("Video encontrado: id={}", id);
            } else {
                log.warn("No se encontró video con id={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("Error buscando video por id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Busca todos los videos de una cámara
    @Override
    public List<VideoDto> findByCameraId(Long cameraId) {
        try {
            List<VideoDto> videos = videoRepository.findByCameraId(cameraId).stream()
                    .map(VideoMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Encontrados {} videos para cameraId={}", videos.size(), cameraId);
            return videos;
        } catch (Exception e) {
            log.error("Error buscando videos por cameraId={}: {}", cameraId, e.getMessage(), e);
            throw e;
        }
    }

    // Busca el video más reciente de una cámara
    @Override
    public Optional<VideoDto> findLatestByCameraId(Long cameraId) {
        try {
            Optional<VideoDto> latest = videoRepository.findTopByCameraIdOrderByCreatedAtDesc(cameraId)
                    .map(VideoMapper::toDto);
            if (latest.isPresent()) {
                log.info("Video más reciente encontrado para cameraId={}", cameraId);
            } else {
                log.warn("No se encontró video reciente para cameraId={}", cameraId);
            }
            return latest;
        } catch (Exception e) {
            log.error("Error buscando video más reciente para cameraId={}: {}", cameraId, e.getMessage(), e);
            throw e;
        }
    }

    // Retorna el recurso físico (archivo) del video
    @Override
    public Resource getVideoResource(Long videoId) {
        try {
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new NotFoundException("Video not found"));
            Path path = Paths.get(video.getFilePath());
            if (!Files.exists(path)) {
                log.warn("El archivo de video no existe en disco: {}", path);
                throw new NotFoundException("Video file not found on disk");
            }
            log.info("Recurso de video listo para descarga: videoId={}", videoId);
            return new FileSystemResource(path);
        } catch (Exception e) {
            log.error("Error accediendo al archivo de video id={}: {}", videoId, e.getMessage(), e);
            throw e;
        }
    }

    // --- Cache de video (opcional, solo si usas Redis/Cache) ---
    @Override
    public Optional<VideoDto> getVideoFromCache(Long cameraId, String videoKey) {
        try {
            Optional<VideoDto> cached = redisService.getVideo(cameraId, videoKey);
            if (cached.isPresent()) {
                log.info("Video obtenido de cache: cameraId={}, key={}", cameraId, videoKey);
            } else {
                log.warn("No se encontró video en cache: cameraId={}, key={}", cameraId, videoKey);
            }
            return cached;
        } catch (Exception e) {
            log.error("Error obteniendo video de cache para cameraId={}, key={}: {}", cameraId, videoKey, e.getMessage(), e);
            return Optional.empty();
        }
    }

    // Retorna todos los videos de la base de datos para una cámara (igual a findByCameraId)
    @Override
    public List<VideoDto> getVideosFromDatabase(Long cameraId) {
        // Alias de findByCameraId, para compatibilidad
        return findByCameraId(cameraId);
    }

    // Registra y cachea un video nuevo (usado desde controller, por ejemplo)
    @Override
    @Transactional
    public VideoDto registerVideo(Long cameraId, VideoDto videoDto, byte[] videoData) {
        try {
            VideoDto videoDtoWithCreated = videoDto.toBuilder()
                    .createdAt(videoDto.getCreatedAt() != null ? videoDto.getCreatedAt() : LocalDateTime.now())
                    .cameraId(cameraId)
                    .build();

            VideoDto saved = saveVideo(videoDtoWithCreated, cameraId, videoData);

            // Cachea el último video si usas Redis
            redisService.cacheVideo(cameraId, "latest", saved);

            log.info("Video registrado y cacheado: id={}, cameraId={}", saved.getId(), cameraId);
            return saved;
        } catch (Exception e) {
            log.error("Error registrando video para cámara id={}: {}", cameraId, e.getMessage(), e);
            throw e;
        }
    }
}
