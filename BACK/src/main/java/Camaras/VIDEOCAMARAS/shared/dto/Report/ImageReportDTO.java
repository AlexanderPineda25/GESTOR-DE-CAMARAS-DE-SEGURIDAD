package Camaras.VIDEOCAMARAS.shared.dto.Report;


import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImageReportDTO {
    private Long id;
    private String filePath;
    private LocalDateTime createdAt;
    private List<ProcessedImageReportDTO> processedImages;
}

