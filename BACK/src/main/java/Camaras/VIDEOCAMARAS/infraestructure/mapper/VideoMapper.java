package Camaras.VIDEOCAMARAS.infraestructure.mapper;

import Camaras.VIDEOCAMARAS.domain.model.Video;
import Camaras.VIDEOCAMARAS.shared.dto.Report.VideoReportDTO;
import Camaras.VIDEOCAMARAS.shared.dto.VideoDto;

public class VideoMapper {
    private VideoMapper() {}

    public static VideoDto toDto(Video video) {
        if (video == null) return null;
        Long cameraId = null;
        String cameraName = null;
        if (video.getCamera() != null) {
            cameraId = video.getCamera().getId();
            cameraName = String.format("%s %s",
                    video.getCamera().getBrand() != null ? video.getCamera().getBrand() : "",
                    video.getCamera().getModel() != null ? video.getCamera().getModel() : ""
            ).trim();
        }
        return VideoDto.builder()
                .id(video.getId())
                .filePath(video.getFilePath())
                .createdAt(video.getCreatedAt())
                .duration(video.getDuration())
                .status(video.getStatus())
                .cameraId(cameraId)
                .cameraName(cameraName)
                .build();
    }

    public static VideoReportDTO toReportDto(Video video) {
        if (video == null) return null;
        return VideoReportDTO.builder()
                .id(video.getId())
                .filePath(video.getFilePath())
                .createdAt(video.getCreatedAt())
                .duration(video.getDuration())
                .status(video.getStatus() != null ? video.getStatus().name() : null)
                .build();
    }

}


