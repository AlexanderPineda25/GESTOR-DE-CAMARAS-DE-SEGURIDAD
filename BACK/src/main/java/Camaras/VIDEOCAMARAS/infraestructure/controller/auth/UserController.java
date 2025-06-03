package Camaras.VIDEOCAMARAS.infraestructure.controller.auth;

import Camaras.VIDEOCAMARAS.aplication.service.CameraService;
import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.UserMapper;
import Camaras.VIDEOCAMARAS.shared.dto.RegisterDto;
import Camaras.VIDEOCAMARAS.shared.dto.Report.UserReportDTO;
import Camaras.VIDEOCAMARAS.shared.dto.CameraDto;
import Camaras.VIDEOCAMARAS.shared.dto.UserPasswordUpdateDto;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import Camaras.VIDEOCAMARAS.shared.dto.UserDto;
import Camaras.VIDEOCAMARAS.aplication.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CameraService cameraService;

    public UserController(UserService userService, CameraService cameraService) {
        this.userService = userService;
        this.cameraService = cameraService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role/{roleType}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable RoleType roleType) {
        return ResponseEntity.ok(userService.findUsersByRole(roleType));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}/details")
    public ResponseEntity<UserReportDTO> getUserDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserReportById(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}/cameras")
    public ResponseEntity<List<CameraDto>> getCamerasByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cameraService.findCamerasByUserId(userId));
    }

    // Nuevo endpoint para crear usuario
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody RegisterDto registerDto) {
        UserDto created = userService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UserDto dto) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        dto.setId(userId);
        UserDto updated = userService.updateUser(dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long userId, @RequestBody UserPasswordUpdateDto dto) {
        userService.updatePassword(dto);
        return ResponseEntity.noContent().build();
    }


    // Nuevo endpoint para eliminar usuario
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);  // Debes implementar este método en UserService
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/report")
    public ResponseEntity<UserReportDTO> getUserReportById(@PathVariable Long userId) {
        try {
            UserReportDTO report = userService.getUserReportById(userId);
            return new ResponseEntity<>(report, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDto> getUserByEmail(@RequestParam String email) {
        User user = userService.findEntityByEmail(email); // Ya existe en tu código
        return ResponseEntity.ok(UserMapper.toDto(user));
    }
}


