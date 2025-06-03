package Camaras.VIDEOCAMARAS.shared.dto.Report;

import java.util.List;
import lombok.*;
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminReportDTO {
    private List<UserReportDTO> users;
}

