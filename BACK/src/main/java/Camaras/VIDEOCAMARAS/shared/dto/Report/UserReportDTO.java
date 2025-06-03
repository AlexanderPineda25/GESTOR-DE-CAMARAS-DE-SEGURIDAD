package Camaras.VIDEOCAMARAS.shared.dto.Report;

import java.util.List;
import lombok.*;
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserReportDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private List<CameraReportDTO> cameras;
}

