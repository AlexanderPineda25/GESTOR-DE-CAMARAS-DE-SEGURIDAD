package Camaras.VIDEOCAMARAS.infraestructure.observer;

import Camaras.VIDEOCAMARAS.domain.model.Camera;

public interface CameraEventListener {
    void onCameraCreated(Camera camera);
    void onCameraUpdated(Camera camera);
    void onCameraDeleted(Camera camera);
}
