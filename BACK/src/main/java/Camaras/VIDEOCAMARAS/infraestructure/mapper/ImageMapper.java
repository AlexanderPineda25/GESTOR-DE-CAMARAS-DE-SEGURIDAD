package Camaras.VIDEOCAMARAS.infraestructure.mapper;

import Camaras.VIDEOCAMARAS.domain.model.Image;
import Camaras.VIDEOCAMARAS.shared.dto.ImageDto;

public class ImageMapper {

    private ImageMapper() {}

    public static ImageDto toDto(Image image) {
        if (image == null) return null;
        String cameraName = null;
        if (image.getCamera() != null) {
            String brand = image.getCamera().getBrand() != null ? image.getCamera().getBrand() : "";
            String model = image.getCamera().getModel() != null ? image.getCamera().getModel() : "";
            cameraName = (brand + " " + model).trim();
        }
        return ImageDto.builder()
                .id(image.getId())
                .filePath(image.getFilePath())
                .createdAt(image.getCreatedAt())
                .cameraId(image.getCamera() != null ? image.getCamera().getId() : null)
                .cameraName(cameraName)
                // .rawImage(image.getData()) // solo si lo necesitas en el DTO
                .build();
    }
}

