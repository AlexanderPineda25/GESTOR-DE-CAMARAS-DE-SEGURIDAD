package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.RolService;
import Camaras.VIDEOCAMARAS.domain.model.Rol;
import Camaras.VIDEOCAMARAS.domain.model.enums.RoleType;
import Camaras.VIDEOCAMARAS.domain.repository.RolRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public Optional<Rol> findByName(RoleType roleType) {
        return rolRepository.findByName(roleType);
    }

    @Override
    @Transactional
    public Rol createRoleIfNotExist(RoleType roleType) {
        return rolRepository.findByName(roleType)
                .orElseGet(() -> rolRepository.save(
                        Rol.builder().name(roleType).build()
                ));
    }

    @Override
    public List<Rol> findAll() {
        return rolRepository.findAll();
    }
}
