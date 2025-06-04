package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.*;
import Camaras.VIDEOCAMARAS.domain.model.Camera;
import Camaras.VIDEOCAMARAS.domain.model.GeoLocation;
import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import Camaras.VIDEOCAMARAS.domain.repository.CameraRepository;
import Camaras.VIDEOCAMARAS.infraestructure.factory.CameraFactory;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.CameraMapper;
import Camaras.VIDEOCAMARAS.shared.dto.*;
import Camaras.VIDEOCAMARAS.shared.exceptions.ForbiddenException;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CameraServiceImpl implements CameraService {

    private final CameraRepository cameraRepository;
    private final UserService userService;
    private final RedisService redisService;
    private static final Logger log = LoggerFactory.getLogger(CameraServiceImpl.class);

    @Autowired
    public CameraServiceImpl(CameraRepository cameraRepository,
                             UserService userService,
                             RedisService redisService) {
        this.cameraRepository = cameraRepository;
        this.userService = userService;
        this.redisService = redisService;
    }

    @Override
    @Transactional
    public CameraResponseDto createCamera(CameraCreateDto cameraCreateDto, String email) {
        try {
            User admin = userService.findEntityByEmail(email);
            if (admin == null || !userService.hasRole(email, RoleType.ADMIN)) {
                log.warn("Intento de crear cámara por usuario no admin: {}", email);
                throw new ForbiddenException("Solo un usuario ADMIN puede crear cámaras");
            }
            if (cameraCreateDto.getUserId() == null) {
                log.warn("No se asignó usuario a la cámara en la creación.");
                throw new IllegalArgumentException("Debe asignar un usuario válido");
            }
            User assignedUser = userService.findEntityById(cameraCreateDto.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuario asignado no encontrado"));

            Camera camera = Camera.builder()
                    .ip(cameraCreateDto.getIp())
                    .user(assignedUser)
                    .brand(cameraCreateDto.getBrand())
                    .model(cameraCreateDto.getModel())
                    .resolution(cameraCreateDto.getResolution())
                    .streamUrl(cameraCreateDto.getStreamUrl())
                    .status(cameraCreateDto.getStatus() != null ? cameraCreateDto.getStatus() : CameraStatus.OFFLINE)
                    .geoLocation(cameraCreateDto.getGeoLocation() != null ?
                            new GeoLocation(cameraCreateDto.getGeoLocation().getLatitude(), cameraCreateDto.getGeoLocation().getLongitude())
                            : null)
                    .registrationDate(LocalDateTime.now())
                    .build();

            Camera saved = cameraRepository.save(camera);
            log.info("Cámara creada exitosamente: id={}, ip={}", saved.getId(), saved.getIp());
            return CameraMapper.toResponseDto(saved);
        } catch (Exception e) {
            log.error("Error creando cámara: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<CameraResponseDto> findById(Long id) {
        try {
            return cameraRepository.findById(id)
                    .map(CameraMapper::toResponseDto);
        } catch (Exception e) {
            log.error("Error al buscar cámara por id={}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public List<CameraResponseDto> getAllCameras() {
        try {
            return cameraRepository.findAll()
                    .stream()
                    .map(CameraMapper::toResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo todas las cámaras: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public List<CameraResponseDto> getCamerasByUser(String email) {
        try {
            User user = userService.findEntityByEmail(email);
            return cameraRepository.findByUserId(user.getId())
                    .stream()
                    .map(CameraMapper::toResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo cámaras por usuario {}: {}", email, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public CameraResponseDto updateCamera(Long id, CameraDto cameraDto, String email) {
        try {
            Camera existingCamera = cameraRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Cámara no encontrada"));

            Camera updatedCamera = CameraFactory.updateCamera(existingCamera, cameraDto);
            Camera saved = cameraRepository.save(updatedCamera);

            log.info("Cámara actualizada: id={}", saved.getId());
            return CameraMapper.toResponseDto(saved);
        } catch (Exception e) {
            log.error("Error actualizando cámara id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteCamera(Long id, String email) {
        try {
            Camera camera = cameraRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Cámara no encontrada"));
            cameraRepository.delete(camera);

            // Elimina los recursos en cache Redis
            redisService.deleteVideoCache(id, "latest");
            redisService.deleteImageCache(id, "latest");

            log.info("Cámara eliminada id={}", id);
        } catch (Exception e) {
            log.error("Error eliminando cámara id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public List<CameraResponseDto> getAllCameraLocations() {
        try {
            return cameraRepository.findAll()
                    .stream()
                    .map(CameraMapper::toResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo ubicaciones de cámaras: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public List<CameraDto> findCamerasByUserId(Long userId) {
        try {
            userService.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + userId));
            return cameraRepository.findByUserId(userId)
                    .stream()
                    .map(CameraMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error encontrando cámaras por userId={}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public Camera findEntityById(Long id) {
        try {
            return cameraRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Cámara no encontrada"));
        } catch (Exception e) {
            log.error("Error buscando entidad cámara id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public Camera createCameraIfNotExists(
            String ip,
            String brand,
            String model,
            String resolution,
            CameraStatus status,
            String streamUrl,
            double latitude,
            double longitude,
            String assignedUserEmail,
            String adminEmail) {
        try {
            if (cameraRepository.findByIp(ip).isPresent()) {
                log.warn("Intento de crear cámara ya existente con IP: {}", ip);
                throw new IllegalArgumentException("La cámara con IP " + ip + " ya existe.");
            }
            User assignedUser = userService.findEntityByEmail(assignedUserEmail);
            if (assignedUser == null) {
                log.warn("Usuario asignado no encontrado: {}", assignedUserEmail);
                throw new NotFoundException("Usuario asignado no encontrado: " + assignedUserEmail);
            }
            User adminUser = userService.findEntityByEmail(adminEmail);
            if (adminUser == null || !userService.hasRole(adminEmail, RoleType.ADMIN)) {
                log.warn("Intento de crear cámara por usuario no admin: {}", adminEmail);
                throw new ForbiddenException("Solo un usuario ADMIN puede crear cámaras");
            }
            GeoLocation geoLocation = GeoLocation.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            Camera camera = Camera.builder()
                    .ip(ip)
                    .brand(brand)
                    .model(model)
                    .resolution(resolution)
                    .status(status != null ? status : CameraStatus.OFFLINE)
                    .streamUrl(streamUrl)
                    .geoLocation(geoLocation)
                    .user(assignedUser)
                    .registrationDate(LocalDateTime.now())
                    .build();

            Camera saved = cameraRepository.save(camera);
            log.info("Cámara creada exitosamente: id={}, ip={}", saved.getId(), saved.getIp());
            return saved;
        } catch (Exception e) {
            log.warn("Error creando cámara (IfNotExists): {}", e.getMessage());
            return null;
        }
    }
}
