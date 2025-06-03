package Camaras.VIDEOCAMARAS.shared.dto.Report;

import java.time.LocalDateTime;
import lombok.*;
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VideoReportDTO {
    private Long id;
    private String filePath;
    private LocalDateTime createdAt;
    private Double duration;
    private String status;
}

