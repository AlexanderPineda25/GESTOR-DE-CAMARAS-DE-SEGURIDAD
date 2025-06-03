package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.RedisService;
import Camaras.VIDEOCAMARAS.shared.dto.ImageDto;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.util.SerializationUtils.serialize;

@Service
public class RedisServiceImpl implements RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.video.ttl.minutes:30}")
    private int videoTtl;

    @Value("${redis.image.ttl.minutes:10}")
    private int imageTtl;

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // --- Helpers para keys ---
    private String videoKey(Long cameraId, String videoKey) {
        return "camera:" + cameraId + ":video:" + videoKey;
    }
    private String imageKey(Long cameraId, String imageKey) {
        return "camera:" + cameraId + ":image:" + imageKey;
    }

    // --- VIDEO ---

    @Override
    public Optional<VideoDto> getVideo(Long cameraId, String videoKey) {
        try {
            Object obj = redisTemplate.opsForValue().get(videoKey(cameraId, videoKey));
            if (obj != null && obj instanceof VideoDto) {
                log.info("Video obtenido de cache: cameraId={}, key={}", cameraId, videoKey);
                return Optional.of((VideoDto) obj);
            } else {
                log.warn("No se encontró video en cache: cameraId={}, key={}", cameraId, videoKey);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error al obtener video de Redis cameraId={}, key={}: {}", cameraId, videoKey, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void cacheVideo(Long cameraId, String videoKey, VideoDto videoDto) {
        try {
            redisTemplate.opsForValue().set(
                    videoKey(cameraId, videoKey),
                    videoDto,
                    videoTtl,
                    TimeUnit.MINUTES
            );
            log.info("Video cacheado: cameraId={}, key={}, ttl={}min", cameraId, videoKey, videoTtl);
        } catch (Exception e) {
            log.error("Error cacheando video cameraId={}, key={}: {}", cameraId, videoKey, e.getMessage(), e);
        }
    }

    @Override
    public void cacheVideos(Map<Long, VideoDto> videos) {
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                videos.forEach((cameraId, videoDto) -> {
                    connection.stringCommands().set(
                            videoKey(cameraId, "latest").getBytes(),
                            serialize(videoDto),
                            Expiration.from(videoTtl, TimeUnit.MINUTES),
                            null
                    );
                });
                return null;
            });
            log.info("Videos cacheados en batch para {} cámaras", videos.size());
        } catch (Exception e) {
            log.error("Error cacheando batch de videos: {}", e.getMessage(), e);
        }
    }

    @Override
    public void deleteVideoCache(Long cameraId, String videoKey) {
        try {
            String key = videoKey(cameraId, videoKey);
            redisTemplate.delete(key);
            log.info("Se eliminó de Redis la clave de video: {}", key);
        } catch (Exception e) {
            log.error("Error al eliminar video de Redis cameraId={}, key={}: {}", cameraId, videoKey, e.getMessage(), e);
        }
    }

    @Override
    public Optional<VideoDto> getVideoFromCache(Long cameraId, String videoKey) {
        // Alias para getVideo, por compatibilidad
        return getVideo(cameraId, videoKey);
    }

    // --- IMAGEN ---

    @Override
    public Optional<ImageDto> getImage(Long cameraId, String imageKey) {
        try {
            Object obj = redisTemplate.opsForValue().get(imageKey(cameraId, imageKey));
            if (obj != null && obj instanceof ImageDto) {
                log.info("Imagen obtenida de cache: cameraId={}, key={}", cameraId, imageKey);
                return Optional.of((ImageDto) obj);
            } else {
                log.warn("No se encontró imagen en cache: cameraId={}, key={}", cameraId, imageKey);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error al obtener imagen de Redis cameraId={}, key={}: {}", cameraId, imageKey, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void cacheImage(Long cameraId, String imageKey, ImageDto imageDto) {
        try {
            redisTemplate.opsForValue().set(
                    imageKey(cameraId, imageKey),
                    imageDto,
                    imageTtl,
                    TimeUnit.MINUTES
            );
            log.info("Imagen cacheada: cameraId={}, key={}, ttl={}min", cameraId, imageKey, imageTtl);
        } catch (Exception e) {
            log.error("Error cacheando imagen cameraId={}, key={}: {}", cameraId, imageKey, e.getMessage(), e);
        }
    }

    @Override
    public void cacheImages(Map<Long, ImageDto> images) {
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                images.forEach((cameraId, imageDto) -> {
                    connection.stringCommands().set(
                            imageKey(cameraId, "latest").getBytes(),
                            serialize(imageDto),
                            Expiration.from(imageTtl, TimeUnit.MINUTES),
                            null
                    );
                });
                return null;
            });
            log.info("Imágenes cacheadas en batch para {} cámaras", images.size());
        } catch (Exception e) {
            log.error("Error cacheando batch de imágenes: {}", e.getMessage(), e);
        }
    }

    @Override
    public void deleteImageCache(Long cameraId, String imageKey) {
        try {
            String key = imageKey(cameraId, imageKey);
            redisTemplate.delete(key);
            log.info("Se eliminó de Redis la clave de imagen: {}", key);
        } catch (Exception e) {
            log.error("Error al eliminar imagen de Redis cameraId={}, key={}: {}", cameraId, imageKey, e.getMessage(), e);
        }
    }

    @Override
    public Optional<ImageDto> getImageFromCache(Long cameraId, String imageKey) {
        // Alias para getImage, por compatibilidad
        return getImage(cameraId, imageKey);
    }
}
