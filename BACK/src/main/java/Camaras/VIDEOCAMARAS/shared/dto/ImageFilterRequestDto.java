package Camaras.VIDEOCAMARAS.shared.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageFilterRequestDto {
    private Long imageId;         // ID de la imagen a procesar
    private Long userId;          // (opcional) ID del usuario que solicita la operación
}
