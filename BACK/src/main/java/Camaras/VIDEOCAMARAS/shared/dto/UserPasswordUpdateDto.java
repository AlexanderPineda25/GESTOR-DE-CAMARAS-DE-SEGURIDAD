package Camaras.VIDEOCAMARAS.shared.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPasswordUpdateDto {
    private Long userId;        // Identifica al usuario
    private String oldPassword; // Opcional, para validar contraseña actual
    private String newPassword; // Nueva contraseña (en texto plano, se debe encriptar en backend)
}
