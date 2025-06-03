package Camaras.VIDEOCAMARAS.shared.dto.Report;

import java.time.LocalDateTime;
import lombok.*;
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedImageReportDTO {
    private Long id;
    private String filePath;
    private LocalDateTime processedAt;
    private String filterType;
}

