package Camaras.VIDEOCAMARAS.infraestructure.observer;

import Camaras.VIDEOCAMARAS.domain.model.Image;

public interface ImageProcessingObserver {
    void onImageProcessed(Image image);
}
