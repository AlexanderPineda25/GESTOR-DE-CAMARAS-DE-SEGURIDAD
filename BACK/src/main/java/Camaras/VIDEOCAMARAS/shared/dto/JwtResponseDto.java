package Camaras.VIDEOCAMARAS.shared.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDto {
    private String token;
    private String tokenType;

    public static JwtResponseDto of(String token) {
        return JwtResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}
