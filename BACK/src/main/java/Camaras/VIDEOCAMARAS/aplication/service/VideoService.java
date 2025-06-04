package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    VideoDto saveVideo(VideoDto videoDto, Long cameraId, byte[] videoData);
    Optional<VideoDto> findById(Long id);
    List<VideoDto> findByCameraId(Long cameraId);
    Optional<VideoDto> findLatestByCameraId(Long cameraId);
    Resource getVideoResource(Long videoId); // Or InputStream if you prefer
    List<VideoDto> getVideosFromDatabase(Long cameraId);
    VideoDto registerVideo(Long cameraId, VideoDto videoDto, byte[] videoData);
    Optional<VideoDto> getVideoFromCache(Long cameraId, String videoKey);
    void reconstruirVideoDesdeRedis(Long cameraId, int startIndex, int endIndex, String outputFilePath) throws Exception;

}
