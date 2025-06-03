package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import Camaras.VIDEOCAMARAS.shared.dto.*;
import Camaras.VIDEOCAMARAS.shared.dto.Report.UserReportDTO;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto register(RegisterDto registerDto);
    JwtResponseDto login(LoginDto loginDto);
    UserDto getLoggedUser(HttpHeaders headers);
    List<UserDto> findUsersByRole(RoleType roleType);
    void registerIfNotExists(String username, String password, String email, RoleType roleType); // Solo si lo necesitas públicamente
    UserDto findByEmail(String email) throws NotFoundException;
    boolean hasRole(String email, RoleType role) throws NotFoundException;
    boolean isOwnerOrAdmin(String email, Long ownerId) throws NotFoundException;
    List<UserDto> findAllUsers();
    UserReportDTO getUserReportById(Long userId);
    User findEntityByEmail(String email) throws NotFoundException;
    Optional<UserDto> findById(Long userId);  // ← Cambiado a Optional
    UserDto updateUser(UserDto userDto);
    void deleteUserById(Long userId);
    void updatePassword(UserPasswordUpdateDto dto);
    Optional<User> findEntityById(Long id);
}

