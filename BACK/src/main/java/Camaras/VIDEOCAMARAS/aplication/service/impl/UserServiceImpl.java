package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.RolService;
import Camaras.VIDEOCAMARAS.aplication.service.UserService;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import Camaras.VIDEOCAMARAS.domain.repository.*;
import Camaras.VIDEOCAMARAS.infraestructure.factory.AuthenticationStrategyFactory;
import Camaras.VIDEOCAMARAS.infraestructure.factory.UserFactory;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.UserMapper;
import Camaras.VIDEOCAMARAS.infraestructure.security.JwtGenerator;
import Camaras.VIDEOCAMARAS.infraestructure.strategy.AuthenticationStrategy;
import Camaras.VIDEOCAMARAS.shared.dto.*;
import Camaras.VIDEOCAMARAS.shared.dto.Report.*;
import Camaras.VIDEOCAMARAS.shared.exceptions.ConflictException;
import Camaras.VIDEOCAMARAS.shared.exceptions.JwtAuthenticationException;
import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import Camaras.VIDEOCAMARAS.domain.model.Rol;
import Camaras.VIDEOCAMARAS.domain.model.User;
import Camaras.VIDEOCAMARAS.domain.repository.CameraRepository;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.VideoMapper;
import Camaras.VIDEOCAMARAS.infraestructure.mapper.ProcessedImageMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RolService rolService;
    private final CameraRepository cameraRepository;
    private final VideoRepository videoRepository;
    private final ImageRepository imageRepository;
    private final ProcessedImageRepository processedImageRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationStrategyFactory authenticationStrategyFactory;

    public UserServiceImpl(
            UserRepository userRepository,
            RolService rolService,
            AuthenticationManager authenticationManager,
            JwtGenerator jwtGenerator,
            PasswordEncoder passwordEncoder,
            AuthenticationStrategyFactory authenticationStrategyFactory,
            CameraRepository cameraRepository,
            VideoRepository videoRepository,
            ImageRepository imageRepository,
            ProcessedImageRepository processedImageRepository
    ) {
        this.userRepository = userRepository;
        this.rolService = rolService;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
        this.passwordEncoder = passwordEncoder;
        this.authenticationStrategyFactory = authenticationStrategyFactory;
        this.cameraRepository = cameraRepository;
        this.videoRepository = videoRepository;
        this.imageRepository = imageRepository;
        this.processedImageRepository = processedImageRepository;
    }


    @Override
    public void registerIfNotExists(String username, String password, String email, RoleType roleType) {
        if (!userRepository.existsByUsername(username)) {
            Rol role = rolService.findByName(roleType)
                    .orElseThrow(() -> new NotFoundException("Rol " + roleType + " no encontrado"));

            String encodedPassword = passwordEncoder.encode(password);

            User user = UserFactory.create(
                    RegisterDto.builder()
                            .username(username)
                            .email(email)
                            .password(password)
                            .roleType(roleType) // si lo necesitas para logging, pero realmente solo lo usas para buscar el rol
                            .build(),
                    role,
                    encodedPassword
            );
            userRepository.save(user);
        }
    }

    @Override
    public UserDto register(RegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new ConflictException("El usuario ya existe!");
        }

        Rol userRole = rolService.findByName(RoleType.USER)
                .orElseThrow(() -> new NotFoundException("Rol USER no encontrado!"));

        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());

        User user = UserFactory.create(registerDto, userRole, encodedPassword);

        userRepository.save(user);

        return UserMapper.toDto(user);
    }

    @Override
    public JwtResponseDto login(LoginDto loginDto) {
        try {
            // Validación explícita de campos
            boolean hasUsername = loginDto.getUsername() != null && !loginDto.getUsername().isEmpty();
            boolean hasEmail = loginDto.getEmail() != null && !loginDto.getEmail().isEmpty();

            if (!hasUsername && !hasEmail) {
                throw new JwtAuthenticationException("Debe proporcionar username o email para autenticación.");
            }
            if (hasUsername && hasEmail) {
                throw new JwtAuthenticationException("Proporcione solo username o solo email, no ambos.");
            }

            String type = hasUsername ? "username" : "email";
            AuthenticationStrategy strategy = authenticationStrategyFactory.getStrategy(type);

            Authentication authentication = strategy.authenticate(loginDto, authenticationManager);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtGenerator.generateToken(authentication);
            return JwtResponseDto.of(token);

        } catch (AuthenticationException e) {
            throw new JwtAuthenticationException("Credenciales inválidas");
        }
    }


    @Override
    public UserDto getLoggedUser(HttpHeaders headers) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String principal = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();

        User user = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal)
                        .orElseThrow(() -> new NotFoundException("Usuario no encontrado")));

        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> findUsersByRole(RoleType roleType) {
        List<User> users = userRepository.findByRole_Name(roleType);
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findByEmail(String email) throws NotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return UserMapper.toDto(user);
    }

    @Override
    public User findEntityByEmail(String email) throws NotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @Override
    public boolean hasRole(String email, RoleType role) throws NotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return user.getRole() != null && user.getRole().getName() == role;
    }

    @Override
    public boolean isOwnerOrAdmin(String email, Long ownerId) throws NotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return (user.getRole() != null && user.getRole().getName() == RoleType.ADMIN)
                || user.getId().equals(ownerId);
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserReportDTO getUserReportById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Supón que tienes mappers y repositorios adecuados
        List<CameraReportDTO> cameras = cameraRepository.findByUserId(userId).stream()
                .map(camera -> {
                    // Mapea videos
                    List<VideoReportDTO> videos = videoRepository.findByCameraId(camera.getId())
                            .stream()
                            .map(VideoMapper::toReportDto) // O tu mapper adecuado
                            .collect(Collectors.toList());

                    // Mapea imágenes (con procesadas)
                    List<ImageReportDTO> images = imageRepository.findByCamera(camera).stream()
                            .map(image -> {
                                List<ProcessedImageReportDTO> processedImages = processedImageRepository.findByOriginalImageId(image.getId()).stream()
                                        .map(ProcessedImageMapper::toReportDto)
                                        .collect(Collectors.toList());

                                return ImageReportDTO.builder()
                                        .id(image.getId())
                                        .filePath(image.getFilePath())
                                        .createdAt(image.getCreatedAt())
                                        .processedImages(processedImages)
                                        .build();
                            }).collect(Collectors.toList());

                    return CameraReportDTO.builder()
                            .id(camera.getId())
                            .ip(camera.getIp())
                            .geoLocation(camera.getGeoLocation())
                            .brand(camera.getBrand())
                            .model(camera.getModel())
                            .resolution(camera.getResolution())
                            .streamUrl(camera.getStreamUrl())
                            .status(camera.getStatus().name())
                            .registrationDate(camera.getRegistrationDate())
                            .videos(videos)
                            .images(images)
                            .build();
                }).collect(Collectors.toList());

        return UserReportDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getName().name()) // O usa RolMapper/toDto según tu modelo
                .cameras(cameras)
                .build();
    }

    @Override
    public Optional<UserDto> findById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toDto);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        if (userDto == null || userDto.getId() == null) {
            throw new IllegalArgumentException("El usuario a actualizar debe tener un ID válido");
        }

        User existingUser = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Actualiza campos permitidos
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());

        // Actualizar rol si viene en DTO y si el rol existe
        if (userDto.getRole() != null && userDto.getRole().getName() != null) {
            try {
                RoleType roleTypeEnum = RoleType.valueOf(userDto.getRole().getName());
                Rol rol = rolService.findByName(roleTypeEnum)
                        .orElseThrow(() -> new NotFoundException("Rol no encontrado"));
                existingUser.setRole(rol);
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Rol inválido: " + userDto.getRole().getName());
            }
        }


        User savedUser = userRepository.save(existingUser);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public void updatePassword(UserPasswordUpdateDto dto) {
        if (dto == null || dto.getUserId() == null) {
            throw new IllegalArgumentException("Debe proveer el ID de usuario y las contraseñas");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Validar contraseña antigua
        if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }

        // Actualizar a nueva contraseña (codificada)
        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        userRepository.delete(user);
    }

    @Override
    public Optional<User> findEntityById(Long id) {
        return userRepository.findById(id);
    }


}