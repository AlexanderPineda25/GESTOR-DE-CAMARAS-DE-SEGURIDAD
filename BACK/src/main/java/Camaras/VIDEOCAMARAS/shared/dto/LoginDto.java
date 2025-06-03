package Camaras.VIDEOCAMARAS.shared.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {

    private String email;
    private String username;
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}
