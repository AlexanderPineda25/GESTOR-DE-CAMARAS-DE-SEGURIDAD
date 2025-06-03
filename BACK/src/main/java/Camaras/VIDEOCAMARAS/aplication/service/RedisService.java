package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.shared.dto.ImageDto;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;

import java.util.Map;
import java.util.Optional;

public interface RedisService {
    Optional<VideoDto> getVideo(Long cameraId, String videoKey);
    Optional<ImageDto> getImage(Long cameraId, String imageKey);

    void cacheVideo(Long cameraId, String videoKey, VideoDto videoDto);
    void cacheImage(Long cameraId, String imageKey, ImageDto imageDto);

    void cacheVideos(Map<Long, VideoDto> videos); // Map<cameraId, VideoDto>
    void cacheImages(Map<Long, ImageDto> images); // Puedes agregar esto para simetría

    void deleteVideoCache(Long cameraId, String videoKey);
    void deleteImageCache(Long cameraId, String imageKey);
    Optional<VideoDto> getVideoFromCache(Long cameraId, String videoKey);
    Optional<ImageDto> getImageFromCache(Long cameraId, String imageKey);
}
