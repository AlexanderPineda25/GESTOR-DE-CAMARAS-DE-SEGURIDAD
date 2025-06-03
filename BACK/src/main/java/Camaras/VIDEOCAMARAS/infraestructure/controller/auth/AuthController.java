package Camaras.VIDEOCAMARAS.infraestructure.controller.auth;

import Camaras.VIDEOCAMARAS.aplication.service.UserService;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.UserMapper;
import Camaras.VIDEOCAMARAS.infraestructure.security.JwtGenerator;
import Camaras.VIDEOCAMARAS.shared.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtGenerator jwtGenerator;

    public AuthController(UserService userService, JwtGenerator jwtGenerator) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        JwtResponseDto jwtResponse = userService.login(loginDto);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterDto registerDto) {
        UserDto userDto = userService.register(registerDto);
        UserResponseDto response = UserMapper.toResponseDto(userDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/api/users/" + userDto.getId())
                .body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponseDto> refreshToken(Authentication authentication) {
        String token = jwtGenerator.refreshToken(authentication);
        return ResponseEntity.ok(JwtResponseDto.of(token));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getAuthenticatedUser(@RequestHeader HttpHeaders headers) {
        UserDto userDto = userService.getLoggedUser(headers);
        UserResponseDto response = UserMapper.toResponseDto(userDto);
        return ResponseEntity.ok(response);
    }
}
