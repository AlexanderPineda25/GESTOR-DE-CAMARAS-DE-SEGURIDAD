package Camaras.VIDEOCAMARAS.shared.dto;

import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 3, message = "La contraseña debe tener al menos 3 caracteres")
    private String password;

    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "El correo no es válido")
    private String email;

    private RoleType roleType; // O Long roleId si lo necesitas

}
