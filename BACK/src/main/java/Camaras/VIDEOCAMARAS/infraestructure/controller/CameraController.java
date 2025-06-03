    package Camaras.VIDEOCAMARAS.infraestructure.controller;

    import Camaras.VIDEOCAMARAS.aplication.service.*;
    import Camaras.VIDEOCAMARAS.aplication.service.impl.ImageProcessingService;
    import Camaras.VIDEOCAMARAS.domain.model.Camera;
    import Camaras.VIDEOCAMARAS.domain.model.GeoLocation;
    import Camaras.VIDEOCAMARAS.domain.model.User;
    import Camaras.VIDEOCAMARAS.domain.model.enums.CameraStatus;
    import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
    import Camaras.VIDEOCAMARAS.domain.repository.CameraRepository;
    import Camaras.VIDEOCAMARAS.infraestructure.mapper.CameraMapper;
    import Camaras.VIDEOCAMARAS.shared.dto.*;
    import Camaras.VIDEOCAMARAS.shared.exceptions.NotFoundException;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.*;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.nio.file.StandardCopyOption;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.UUID;

    @RestController
    @RequestMapping("/api/cameras")
    public class CameraController {

        private final CameraService cameraService;
        private final UserService userService; // Asumiendo que tienes un UserService para manejar usuarios
        private final CameraRepository cameraRepository; // Asumiendo que tienes un repositorio para manejar cámaras

        @Autowired
        public CameraController(CameraService cameraService,
                                UserService userService,
                                CameraRepository cameraRepository) {
            this.cameraService = cameraService;
            this.userService = userService; // Inyectar el UserService
            this.cameraRepository = cameraRepository; // Inyectar el repositorio de cámaras
        }

        @PostMapping("/create")
        public ResponseEntity<CameraResponseDto> createCamera(@RequestBody CameraCreateDto cameraCreateDto) {
            Long userId = cameraCreateDto.getUserId();
            User user = userService.findEntityById(userId)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + userId));

            // Construir la cámara
            Camera camera = Camera.builder()
                    .ip(cameraCreateDto.getIp())
                    .user(user)  // Usuario asignado
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

            cameraRepository.save(camera);  // Guardar la cámara en la base de datos
            return ResponseEntity.status(HttpStatus.CREATED).body(CameraMapper.toResponseDto(camera));
        }

        @GetMapping
        public ResponseEntity<List<CameraResponseDto>> getAllCameras() {
            return ResponseEntity.ok(cameraService.getAllCameras());
        }

        @GetMapping("/by-user")
        public ResponseEntity<List<CameraResponseDto>> getCamerasByUser(@RequestParam String email) {
            return ResponseEntity.ok(cameraService.getCamerasByUser(email));
        }

        @PutMapping("/{id}")
        public ResponseEntity<CameraResponseDto> updateCamera(@PathVariable Long id,
                                                              @RequestBody CameraDto cameraDto,
                                                              @RequestParam String email) {
            return ResponseEntity.ok(cameraService.updateCamera(id, cameraDto, email));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteCamera(@PathVariable Long id, @RequestParam String email) {
            cameraService.deleteCamera(id, email);
            return ResponseEntity.noContent().build();
        }


        @GetMapping("/locations")
        public ResponseEntity<List<CameraResponseDto>> getAllCameraLocations() {
            return ResponseEntity.ok(cameraService.getAllCameraLocations());
        }

        @GetMapping(value = "/{cameraId}/stream-url", produces = MediaType.TEXT_PLAIN_VALUE)
        public ResponseEntity<String> getStreamUrl(
                @PathVariable Long cameraId,
                @RequestParam(required = true) String email) {

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El parámetro email es obligatorio");
            }

            // Buscar la cámara en la base de datos
            Camera camera = cameraRepository.findById(cameraId)
                    .orElse(null);

            if (camera == null || camera.getStreamUrl() == null || camera.getStreamUrl().isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró la cámara o la URL de stream no está configurada");
            }

            // Retornar la URL real de la cámara (ejemplo: "http://192.168.1.4:8080/video")
            return ResponseEntity.ok(camera.getStreamUrl());
        }


        private String saveTempImage(MultipartFile file) throws IOException {
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                throw new IllegalArgumentException("El archivo debe ser una imagen");
            }

            String extension = contentType.split("/")[1];
            String filename = String.format("capture_%s_%d.%s",
                    UUID.randomUUID().toString().substring(0, 8),
                    System.currentTimeMillis(),
                    extension);

            Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), filename);
            Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);

            return tempPath.toString();
        }
    }