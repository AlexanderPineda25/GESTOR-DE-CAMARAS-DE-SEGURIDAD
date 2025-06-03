package Camaras.VIDEOCAMARAS.infraestructure.observer;

import Camaras.VIDEOCAMARAS.domain.model.Camera;

import java.util.ArrayList;
import java.util.List;

public class CameraEventPublisher {
    private final List<CameraEventListener> listeners = new ArrayList<>();

    public void addListener(CameraEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CameraEventListener listener) {
        listeners.remove(listener);
    }

    public void notifyCreated(Camera camera) {
        for (CameraEventListener listener : listeners) {
            listener.onCameraCreated(camera);
        }
    }

    public void notifyUpdated(Camera camera) {
        for (CameraEventListener listener : listeners) {
            listener.onCameraUpdated(camera);
        }
    }

    public void notifyDeleted(Camera camera) {
        for (CameraEventListener listener : listeners) {
            listener.onCameraDeleted(camera);
        }
    }
}

