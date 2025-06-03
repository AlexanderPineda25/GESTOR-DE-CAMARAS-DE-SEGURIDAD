package Camaras.VIDEOCAMARAS.infraestructure.config;

import Camaras.VIDEOCAMARAS.aplication.service.CameraService;
import Camaras.VIDEOCAMARAS.aplication.service.RolService;
import Camaras.VIDEOCAMARAS.aplication.service.UserService;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class UserInitializer {

    private final RolService rolService;
    private final UserService userService;
    private final CameraService cameraService;

    public UserInitializer(RolService rolService, UserService userService, CameraService cameraService) {
        this.rolService = rolService;
        this.userService = userService;
        this.cameraService = cameraService;
    }

    @PostConstruct
    public void initDefaults() {
        for (RoleType roleType : RoleType.values()) {
            rolService.createRoleIfNotExist(roleType);
        }

        userService.registerIfNotExists("admin", "admin", "admin@example.com", RoleType.ADMIN);
        userService.registerIfNotExists("user", "user", "user@example.com", RoleType.USER);
        System.out.println("Default roles and users initialized.");
    }


}
